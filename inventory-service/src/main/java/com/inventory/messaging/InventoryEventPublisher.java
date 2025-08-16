package com.inventory.messaging;

import com.inventory.messaging.events.InventoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.topics.inventory-events:inventory-events}")
    private String topic;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(InventoryEvent event) {
        try {
            if (event == null) {
                log.warn("InventoryEvent is null; skipping publish");
                return;
            }

            String resolvedTopic = this.topic;
            if (resolvedTopic == null || resolvedTopic.isBlank()) {
                resolvedTopic = "inventory-events";
            }

            String key = null;
            String sku = event.getSku();
            if (sku != null && !sku.isBlank()) {
                key = sku;
            } else {
                UUID id = event.getId();
                if (id != null) {
                    key = id.toString();
                }
            }
            if (key == null) {
                key = UUID.randomUUID().toString();
            }

            kafkaTemplate.send(resolvedTopic, key, event);
            log.info("Published InventoryEvent type={} id={} sku={} to topic {}", event.getType(), event.getId(), event.getSku(), resolvedTopic);
        } catch (Exception e) {
            log.error("Failed to publish InventoryEvent: {}", e.getMessage(), e);
        }
    }
}
