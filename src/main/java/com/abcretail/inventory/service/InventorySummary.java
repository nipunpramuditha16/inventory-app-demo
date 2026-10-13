package com.abcretail.inventory.service;

import java.math.BigDecimal;

public record InventorySummary(long totalProducts, int totalUnits, BigDecimal totalValue, long lowStockCount) {
}
