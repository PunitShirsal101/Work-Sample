package com.example.order.api;

import com.example.common.events.OrderCreatedEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Log log = LogFactory.getLog(OrderController.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody OrderCreatedEvent event) {
        // Async to notification-service via Kafka (topic: orders.created)
        kafkaTemplate.send("orders.created", event.getOrderId(), event);
        log.info("Published OrderCreatedEvent for orderId=" + event.getOrderId());
        // Sync call to inventory would occur here; keeping as placeholder.
        return ResponseEntity.ok(event.getOrderId());
    }
}
