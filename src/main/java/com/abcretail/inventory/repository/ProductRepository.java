package com.abcretail.inventory.repository;

import com.abcretail.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("select p from Product p where p.quantity <= p.reorderLevel order by p.quantity")
    List<Product> findLowStock();
}
