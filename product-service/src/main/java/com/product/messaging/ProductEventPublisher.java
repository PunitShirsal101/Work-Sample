package com.product.messaging;

import com.product.messaging.events.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class ProductEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.topics.product-events:product-events}")
    private String topic;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ProductEvent event) {
        try {
            if (event == null) {
                log.warn("ProductEvent is null; skipping publish");
                return;
            }

            String resolvedTopic = this.topic;
            if (resolvedTopic == null || resolvedTopic.isBlank()) {
                resolvedTopic = "product-events";
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
            // Guarantee non-null key for Kafka sends it to satisfy static analysis
            if (key == null) {
                key = UUID.randomUUID().toString();
            }

            kafkaTemplate.send(resolvedTopic, key, event);
            log.info("Published ProductEvent type={} id={} sku={} to topic {}", event.getType(), event.getId(), event.getSku(), resolvedTopic);
        } catch (Exception e) {
            log.error("Failed to publish ProductEvent: {}", e.getMessage(), e);
        }
    }
}
