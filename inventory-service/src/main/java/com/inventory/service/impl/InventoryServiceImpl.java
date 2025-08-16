package com.inventory.service.impl;

import com.inventory.api.dto.InventoryDeductRequest;
import com.inventory.domain.InventoryItem;
import com.inventory.messaging.InventoryEventPublisher;
import com.inventory.messaging.events.InventoryEvent;
import com.inventory.repository.InventoryRepository;
import com.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository repository;
    private final InventoryEventPublisher eventPublisher;
    private final InventoryService self;

    public InventoryServiceImpl(InventoryRepository repository, InventoryEventPublisher eventPublisher, @Lazy InventoryService self) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.self = self;
    }

    @Override
    public boolean isAvailable(String sku, int qty) {
        Integer available = self.getAvailableQuantity(sku);
        return available != null && available >= qty;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "inventoryAvailability", allEntries = true)
    public void deduct(InventoryDeductRequest request) {
        applyDelta(request, -1);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "inventoryAvailability", allEntries = true)
    public void restore(InventoryDeductRequest request) {
        applyDelta(request, +1);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "inventoryAvailability", allEntries = true)
    public void restock(InventoryDeductRequest request) {
        applyDelta(request, +1);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "inventoryAvailability", allEntries = true)
    public void bulkImport(InventoryDeductRequest request) {
        // For bulk import, create missing SKUs and set quantity to item.quantity (not delta)
        if (request == null || request.getItems() == null) return;
        for (var item : request.getItems()) {
            retryOptimistic(() -> {
                InventoryItem entity = repository.findById(item.getSku())
                        .orElseGet(() -> new InventoryItem(item.getSku(), 0));
                entity.setQuantity(item.getQuantity());
                repository.save(entity);
                checkLowStockAndAlert(entity);
            });
        }
    }

    @Override
    @Cacheable(cacheNames = "inventoryAvailability", key = "#sku")
    public Integer getAvailableQuantity(String sku) {
        return repository.findById(sku).map(InventoryItem::getQuantity).orElse(0);
    }

    private void applyDelta(InventoryDeductRequest request, int sign) {
        if (request == null || request.getItems() == null) return;
        Map<String, Integer> aggregated = new HashMap<>();
        for (var item : request.getItems()) {
            aggregated.merge(item.getSku(), item.getQuantity() * sign, Integer::sum);
        }
        for (var entry : aggregated.entrySet()) {
            String sku = entry.getKey();
            int delta = entry.getValue();
            retryOptimistic(() -> {
                InventoryItem entity = repository.findById(sku)
                        .orElseGet(() -> new InventoryItem(sku, 0));
                int newQty = entity.getQuantity() + delta;
                if (newQty < 0) {
                    throw new IllegalStateException("Insufficient stock for sku=" + sku);
                }
                entity.setQuantity(newQty);
                repository.save(entity);
                checkLowStockAndAlert(entity);
            });
        }
    }

    private void retryOptimistic(Runnable action) {
        int attempts = 0;
        while (true) {
            try {
                action.run();
                return;
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= 3) throw e;
            }
        }
    }

    private void checkLowStockAndAlert(InventoryItem entity) {
        if (entity.getQuantity() <= entity.getLowStockThreshold()) {
            log.warn("Low stock alert for sku={} quantity={}", entity.getSku(), entity.getQuantity());
            // Publish a low-stock event (structure mirrors ProductEventPublisher)
            try {
                InventoryEvent event = InventoryEvent.of(InventoryEvent.Type.LOW_STOCK, null, entity.getSku(), entity.getQuantity());
                eventPublisher.publish(event);
            } catch (Exception e) {
                // Keep service flow intact on publish failures
                log.debug("Low-stock publish threw exception: {}", e.toString());
            }
        }
    }
}
