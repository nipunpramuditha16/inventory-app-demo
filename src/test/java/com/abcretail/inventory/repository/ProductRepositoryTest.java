package com.abcretail.inventory.repository;

import com.abcretail.inventory.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void findBySkuReturnsSavedProduct() {
        repository.save(new Product("Desk Lamp", "HOME-001", "Home", 7, new BigDecimal("3200.00"), 3));

        assertTrue(repository.findBySku("HOME-001").isPresent());
        assertTrue(repository.existsBySku("HOME-001"));
    }

    @Test
    void findLowStockReturnsOnlyItemsAtOrBelowReorderLevel() {
        repository.save(new Product("Plenty", "T-1", "Test", 50, new BigDecimal("1.00"), 10));
        repository.save(new Product("Exactly at level", "T-2", "Test", 10, new BigDecimal("1.00"), 10));
        repository.save(new Product("Nearly out", "T-3", "Test", 1, new BigDecimal("1.00"), 10));

        List<Product> low = repository.findLowStock();

        assertEquals(2, low.size());
        assertEquals("T-3", low.get(0).getSku()); // ordered by quantity ascending
        assertEquals("T-2", low.get(1).getSku());
    }
}
