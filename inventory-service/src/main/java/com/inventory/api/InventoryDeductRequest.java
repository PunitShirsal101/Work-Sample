package com.inventory.api;

import java.math.BigDecimal;
import java.util.List;

public class InventoryDeductRequest {
    public static class Item {
        private String sku;
        private int quantity;
        private BigDecimal price;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    private List<Item> items;

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}