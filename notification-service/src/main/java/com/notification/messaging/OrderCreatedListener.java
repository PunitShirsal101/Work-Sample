package com.notification.messaging;

import com.common.events.OrderCreatedEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {
    private static final Log log = LogFactory.getLog(OrderCreatedListener.class);

    @KafkaListener(topics = "orders.created", groupId = "notification-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        // Placeholder: emulate sending email/notification
        log.info("[Notification] Order created: " + event.getOrderId() + ", user=" + event.getUserId());
    }
}
