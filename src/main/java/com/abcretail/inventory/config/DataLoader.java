package com.abcretail.inventory.config;

import com.abcretail.inventory.model.Product;
import com.abcretail.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

/** Loads sample products into the in-memory database at startup (not used by the "test" profile). */
@Configuration
public class DataLoader {

    @Bean
    @Profile("!test")
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.saveAll(List.of(
                    new Product("Wireless Mouse", "ELEC-001", "Electronics", 45, new BigDecimal("2500.00"), 10),
                    new Product("USB-C Cable 1m", "ELEC-002", "Electronics", 8, new BigDecimal("750.00"), 15),
                    new Product("Mechanical Keyboard", "ELEC-003", "Electronics", 20, new BigDecimal("12500.00"), 5),
                    new Product("A4 Paper Ream", "STAT-001", "Stationery", 120, new BigDecimal("1200.00"), 30),
                    new Product("Ballpoint Pens (Box of 50)", "STAT-002", "Stationery", 5, new BigDecimal("1800.00"), 10),
                    new Product("Office Chair", "FURN-001", "Furniture", 12, new BigDecimal("28000.00"), 4)
            ));
        };
    }
}
