package com.product.messaging.events;

import java.time.Instant;
import java.util.UUID;

public class ProductEvent {
    public enum Type { CREATED, UPDATED, DELETED }

    private Type type;
    private UUID id;
    private String sku;
    private Instant timestamp;

    public ProductEvent() {}

    public ProductEvent(Type type, UUID id, String sku, Instant timestamp) {
        this.type = type;
        this.id = id;
        this.sku = sku;
        this.timestamp = timestamp;
    }

    public static ProductEvent of(Type type, UUID id, String sku) {
        return new ProductEvent(type, id, sku, Instant.now());
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
