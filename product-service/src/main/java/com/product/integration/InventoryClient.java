package com.product.integration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class InventoryClient {
    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);
    private final RestTemplate restTemplate;

    @Value("${inventory-service.base-url:http://localhost:8083}")
    private String baseUrl;

    public InventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "isAvailableFallback")
    public boolean isAvailable(String sku, int qty) {
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/inventory/check")
                .queryParam("sku", sku)
                .queryParam("qty", qty)
                .toUriString();
        Boolean result = restTemplate.getForObject(uri, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    // Fallback signature must match original + Throwable
    @SuppressWarnings("java:S3400") // Sonar: method returns constant by design as a conservative fallback
    private boolean isAvailableFallback(String sku, int qty, Throwable t) {
        // Conservative fallback: consider unavailable if inventory service is down
        log.warn("Inventory availability check failed for sku={} qty={}, returning unavailable.", sku, qty, t);
        return false;
    }
}