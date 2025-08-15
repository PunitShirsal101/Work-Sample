package com.common.events;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreatedEvent {
    public static class Item {
        private String sku;
        private int quantity;
        private BigDecimal price;

        public Item() {}
        public Item(String sku, int quantity, BigDecimal price) {
            this.sku = sku;
            this.quantity = quantity;
            this.price = price;
        }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    private String orderId;
    private String userId;
    private List<Item> items;
    private BigDecimal total;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String orderId, String userId, List<Item> items, BigDecimal total) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.total = total;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
