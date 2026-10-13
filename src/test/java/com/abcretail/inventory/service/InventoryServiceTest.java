package com.abcretail.inventory.service;

import com.abcretail.inventory.exception.DuplicateSkuException;
import com.abcretail.inventory.exception.InsufficientStockException;
import com.abcretail.inventory.exception.ProductNotFoundException;
import com.abcretail.inventory.model.Product;
import com.abcretail.inventory.repository.ProductRepository;
import com.abcretail.inventory.util.Calculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository repository;

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(repository, new Calculator());
    }

    private Product product(Long id, String sku, int qty, String price, int reorder) {
        Product p = new Product("Item " + sku, sku, "General", qty, new BigDecimal(price), reorder);
        p.setId(id);
        return p;
    }

    @Test
    void findByIdReturnsProduct() {
        Product p = product(1L, "A-1", 10, "5.00", 2);
        when(repository.findById(1L)).thenReturn(Optional.of(p));

        assertEquals("A-1", service.findById(1L).getSku());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void createSavesNewProduct() {
        Product p = product(null, "A-1", 10, "5.00", 2);
        when(repository.existsBySku("A-1")).thenReturn(false);
        when(repository.save(p)).thenReturn(p);

        assertEquals(p, service.create(p));
        verify(repository).save(p);
    }

    @Test
    void createRejectsDuplicateSku() {
        Product p = product(null, "A-1", 10, "5.00", 2);
        when(repository.existsBySku("A-1")).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> service.create(p));
        verify(repository, never()).save(any());
    }

    @Test
    void adjustStockIncreasesQuantity() {
        Product p = product(1L, "A-1", 10, "5.00", 2);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(p)).thenReturn(p);

        assertEquals(15, service.adjustStock(1L, 5).getQuantity());
    }

    @Test
    void adjustStockDecreasesQuantity() {
        Product p = product(1L, "A-1", 10, "5.00", 2);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(p)).thenReturn(p);

        assertEquals(4, service.adjustStock(1L, -6).getQuantity());
    }

    @Test
    void adjustStockRejectsNegativeResult() {
        Product p = product(1L, "A-1", 3, "5.00", 2);
        when(repository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(InsufficientStockException.class, () -> service.adjustStock(1L, -4));
        verify(repository, never()).save(any());
    }

    @Test
    void lowStockFlagUsesReorderLevel() {
        assertTrue(product(1L, "A-1", 2, "5.00", 2).isLowStock());
        assertTrue(product(2L, "A-2", 1, "5.00", 2).isLowStock());
        assertFalse(product(3L, "A-3", 3, "5.00", 2).isLowStock());
    }

    @Test
    void summarizeComputesTotals() {
        when(repository.findAll()).thenReturn(List.of(
                product(1L, "A-1", 10, "5.00", 2),    // value 50.00
                product(2L, "A-2", 2, "100.50", 5)));  // value 201.00, low stock

        InventorySummary summary = service.summarize();

        assertEquals(2, summary.totalProducts());
        assertEquals(12, summary.totalUnits());
        assertEquals(new BigDecimal("251.00"), summary.totalValue());
        assertEquals(1, summary.lowStockCount());
    }
}
