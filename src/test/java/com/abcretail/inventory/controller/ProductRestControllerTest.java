package com.abcretail.inventory.controller;

import com.abcretail.inventory.controller.api.ProductRestController;
import com.abcretail.inventory.exception.GlobalExceptionHandler;
import com.abcretail.inventory.exception.InsufficientStockException;
import com.abcretail.inventory.exception.ProductNotFoundException;
import com.abcretail.inventory.model.Product;
import com.abcretail.inventory.service.InventoryService;
import com.abcretail.inventory.service.InventorySummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductRestController.class)
@Import(GlobalExceptionHandler.class)
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService service;

    private Product sample() {
        Product p = new Product("Wireless Mouse", "ELEC-001", "Electronics", 45, new BigDecimal("2500.00"), 10);
        p.setId(1L);
        return p;
    }

    @Test
    void listReturnsProducts() throws Exception {
        when(service.findAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sku").value("ELEC-001"))
                .andExpect(jsonPath("$[0].lowStock").value(false));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        when(service.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product not found with id 99"));
    }

    @Test
    void summaryReturnsTotals() throws Exception {
        when(service.summarize()).thenReturn(new InventorySummary(3, 70, new BigDecimal("1000.00"), 1));

        mockMvc.perform(get("/api/products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(3))
                .andExpect(jsonPath("$.lowStockCount").value(1));
    }

    @Test
    void createReturns201() throws Exception {
        when(service.create(any(Product.class))).thenReturn(sample());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Wireless Mouse","sku":"ELEC-001","category":"Electronics",
                                 "quantity":45,"price":2500.00,"reorderLevel":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","sku":"","quantity":-1,"price":5.00,"reorderLevel":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void adjustStockReturns409WhenInsufficient() throws Exception {
        when(service.adjustStock(1L, -100)).thenThrow(new InsufficientStockException("ELEC-001", 45, 100));

        mockMvc.perform(patch("/api/products/1/stock").param("delta", "-100"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
