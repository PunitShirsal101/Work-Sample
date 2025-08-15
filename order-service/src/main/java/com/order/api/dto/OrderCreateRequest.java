package com.order.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreateRequest {
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

    private String userId;
    private List<Item> items;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}