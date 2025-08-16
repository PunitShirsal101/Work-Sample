package com.inventory.service;

import com.inventory.api.dto.InventoryDeductRequest;

public interface InventoryService {
    boolean isAvailable(String sku, int qty);
    void deduct(InventoryDeductRequest request);
    void restore(InventoryDeductRequest request);
    void restock(InventoryDeductRequest request);
    void bulkImport(InventoryDeductRequest request);
    Integer getAvailableQuantity(String sku);
}