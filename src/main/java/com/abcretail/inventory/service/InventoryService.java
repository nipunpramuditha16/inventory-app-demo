package com.abcretail.inventory.service;

import com.abcretail.inventory.exception.DuplicateSkuException;
import com.abcretail.inventory.exception.InsufficientStockException;
import com.abcretail.inventory.exception.ProductNotFoundException;
import com.abcretail.inventory.model.Product;
import com.abcretail.inventory.repository.ProductRepository;
import com.abcretail.inventory.util.Calculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository repository;
    private final Calculator calculator;

    public InventoryService(ProductRepository repository, Calculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> findLowStock() {
        return repository.findLowStock();
    }

    public Product create(Product product) {
        if (repository.existsBySku(product.getSku())) {
            throw new DuplicateSkuException(product.getSku());
        }
        product.setId(null);
        return repository.save(product);
    }

    public Product update(Long id, Product changes) {
        Product existing = findById(id);
        repository.findBySku(changes.getSku()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DuplicateSkuException(changes.getSku());
            }
        });
        existing.setName(changes.getName());
        existing.setSku(changes.getSku());
        existing.setCategory(changes.getCategory());
        existing.setQuantity(changes.getQuantity());
        existing.setPrice(changes.getPrice());
        existing.setReorderLevel(changes.getReorderLevel());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Product existing = findById(id);
        repository.delete(existing);
    }

    /**
     * Adds (positive delta) or removes (negative delta) stock.
     *
     * @throws InsufficientStockException if the resulting quantity would be negative
     */
    public Product adjustStock(Long id, int delta) {
        Product product = findById(id);
        int newQuantity = calculator.add(product.getQuantity(), delta);
        if (newQuantity < 0) {
            throw new InsufficientStockException(product.getSku(), product.getQuantity(), -delta);
        }
        product.setQuantity(newQuantity);
        return repository.save(product);
    }

    @Transactional(readOnly = true)
    public InventorySummary summarize() {
        List<Product> products = repository.findAll();
        int totalUnits = products.stream()
                .mapToInt(Product::getQuantity)
                .reduce(0, calculator::add);
        BigDecimal totalValue = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long lowStock = products.stream().filter(Product::isLowStock).count();
        return new InventorySummary(products.size(), totalUnits, totalValue, lowStock);
    }
}
