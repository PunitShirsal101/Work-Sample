package com.inventory.messaging.events;

import java.time.Instant;
import java.util.UUID;

public class InventoryEvent {
    public enum Type { LOW_STOCK }

    private Type type;
    private UUID id;
    private String sku;
    private Integer quantity;
    private Instant timestamp;

    public InventoryEvent() {}

    public InventoryEvent(Type type, UUID id, String sku, Integer quantity, Instant timestamp) {
        this.type = type;
        this.id = id;
        this.sku = sku;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public static InventoryEvent of(Type type, UUID id, String sku, Integer quantity) {
        return new InventoryEvent(type, id, sku, quantity, Instant.now());
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
