package com.order.service.impl;

import com.common.events.OrderCreatedEvent;
import com.order.api.dto.OrderCreateRequest;
import com.order.domain.Order;
import com.order.domain.OrderStatus;
import com.order.repository.OrderRepository;
import com.order.service.OrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.order.integration.InventoryClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.order.logging.OrderLogMessages.*;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Log log = LogFactory.getLog(OrderServiceImpl.class);

    // In-memory idempotency index retained for minimal change
    private final Map<String, String> idempotencyIndex = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public OrderServiceImpl(RestTemplate restTemplate, KafkaTemplate<String, Object> kafkaTemplate, OrderRepository orderRepository, InventoryClient inventoryClient) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @Transactional
    public Order create(OrderCreateRequest request, String idempotencyKey) {
        Optional<Order> existing = findIdempotentOrder(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        Order order = initOrderAndPersist(request, idempotencyKey);

        boolean deducted = false;
        boolean paymentCharged = false;
        try {
            if (!isInventoryAllAvailable(order.getItems())) {
                return markFailedAndSave(order);
            }

            paymentCharged = chargePaymentOrWarn(order);
            if (!paymentCharged) {
                return markFailedAndSave(order);
            }

            deducted = deductInventoryOrCompensate(order, paymentCharged);
            if (!deducted) {
                return markFailedAndSave(order);
            }

            order.setStatus(OrderStatus.CONFIRMED);
            publishOrderCreatedEvent(order);
            return orderRepository.save(order);
        } catch (RestClientException e) {
            logErrorWithOrderId(MSG_EXTERNAL_CALL_FAILED_DURING_CREATE, order.getId(), e);
            order.setStatus(OrderStatus.FAILED);
            if (deducted) {
                handleInventoryRestoreOnFailure(order.getItems(), order.getId(), MSG_INV_RESTORE_FAIL_DURING_COMP, MSG_INV_RESTORE_SUCCESS_DURING_COMP, MSG_INV_RESTORE_CALL_FAILED_DURING_COMP);
            }
            if (paymentCharged) {
                handleRefundOnFailure(order.getId(), order.getTotal(), MSG_REFUND_FAILED_DURING_EXCEPTION, MSG_REFUND_SUCCEEDED_DURING_EXCEPTION);
            }
            return orderRepository.save(order);
        } catch (RuntimeException e) {
            logErrorWithOrderId(MSG_UNEXPECTED_ERROR_DURING_CREATE, order.getId(), e);
            order.setStatus(OrderStatus.FAILED);
            if (deducted) {
                handleInventoryRestoreOnFailure(order.getItems(), order.getId(), MSG_INV_RESTORE_FAIL_DURING_UNEXPECTED, MSG_INV_RESTORE_SUCCESS_DURING_UNEXPECTED, MSG_INV_RESTORE_CALL_FAILED_DURING_UNEXPECTED);
            }
            if (paymentCharged) {
                handleRefundOnFailure(order.getId(), order.getTotal(), MSG_REFUND_FAILED_DURING_UNEXPECTED, MSG_REFUND_SUCCEEDED_DURING_UNEXPECTED);
            }
            return orderRepository.save(order);
        }
    }

    private Optional<Order> findIdempotentOrder(String idempotencyKey) {
        if (idempotencyKey == null) return Optional.empty();
        String existingOrderId = idempotencyIndex.get(idempotencyKey);
        if (existingOrderId == null) return Optional.empty();
        return orderRepository.findById(existingOrderId);
    }

    private Order initOrderAndPersist(OrderCreateRequest request, String idempotencyKey) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(request.getUserId());
        var items = request.getItems().stream()
                .map(i -> new Order.Item(i.getSku(), i.getQuantity(), i.getPrice()))
                .toList();
        order.setItems(items);
        order.setTotal(items.stream()
                .map(it -> it.getPrice().multiply(BigDecimal.valueOf(it.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        if (idempotencyKey != null) {
            idempotencyIndex.putIfAbsent(idempotencyKey, order.getId());
        }
        return order;
    }

    private boolean isInventoryAllAvailable(java.util.List<Order.Item> items) {
        return items.stream().allMatch(it -> inventoryClient.isAvailable(it.getSku(), it.getQuantity()));
    }

    private boolean chargePaymentOrWarn(Order order) {
        boolean charged = simulatePaymentCharge(order.getUserId(), order.getTotal());
        if (!charged && log.isWarnEnabled()) {
            log.warn(MSG_PAYMENT_FAILED_FOR_ORDER + order.getId());
        }
        return charged;
    }

    private boolean deductInventoryOrCompensate(Order order, boolean paymentCharged) {
        boolean deducted = inventoryClient.deduct(order.getItems());
        if (!deducted && paymentCharged) {
            boolean refunded = simulatePaymentRefund(order.getId(), order.getTotal());
            if (!refunded) {
                if (log.isErrorEnabled()) {
                    log.error(MSG_REFUND_FAILED_AFTER_DEDUCT_FAIL + order.getId());
                }
            } else {
                if (log.isInfoEnabled()) {
                    log.info(MSG_REFUND_SUCCEEDED_AFTER_DEDUCT_FAIL + order.getId());
                }
            }
        }
        return deducted;
    }

    private void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getItems().stream()
                        .map(i -> new OrderCreatedEvent.Item(i.getSku(), i.getQuantity(), i.getPrice()))
                        .toList(),
                order.getTotal()
        );
        kafkaTemplate.send("orders.created", order.getId(), event);
        if (log.isInfoEnabled()) {
            log.info(MSG_PUBLISHED_ORDER_CREATED_EVENT + order.getId());
        }
    }

    private Order markFailedAndSave(Order order) {
        order.setStatus(OrderStatus.FAILED);
        return orderRepository.save(order);
    }
    
    private void logErrorWithOrderId(String baseMsg, String orderId, Throwable e) {
        log.error(baseMsg + orderId, e);
    }

    private void handleInventoryRestoreOnFailure(java.util.List<Order.Item> items,
                                                 String orderId,
                                                 String msgFail,
                                                 String msgSuccess,
                                                 String msgCallFailed) {
        try {
            boolean restored = inventoryClient.restore(items);
            if (!restored) {
                log.warn(msgFail + orderId);
            } else {
                log.info(msgSuccess + orderId);
            }
        } catch (RestClientException ex2) {
            log.error(msgCallFailed + orderId, ex2);
        }
    }

    private void handleRefundOnFailure(String orderId,
                                       BigDecimal amount,
                                       String msgFail,
                                       String msgSuccess) {
        boolean refunded = simulatePaymentRefund(orderId, amount);
        if (!refunded) {
            log.error(msgFail + orderId);
        } else {
            log.info(msgSuccess + orderId);
        }
    }

    private boolean simulatePaymentCharge(String userId, BigDecimal amount) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                if (log.isInfoEnabled()) {
                    log.info(MSG_SKIP_PAYMENT_CHARGE_NON_POSITIVE + amount);
                }
                return true;
            }
            var body = new java.util.HashMap<String, Object>();
            body.put("userId", userId);
            body.put("amount", amount);
            Boolean result = restTemplate.postForObject("http://localhost:8084/payment/charge", body, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RestClientException ex) {
            if (log.isErrorEnabled()) {
                log.error(MSG_PAYMENT_SERVICE_CALL_FAILED_FOR_USER + userId + FRAG_COMMA_AMOUNT + amount, ex);
            }
            return false;
        }
    }

    private boolean simulatePaymentRefund(String orderId, BigDecimal amount) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.info(MSG_SKIP_PAYMENT_REFUND_NON_POSITIVE + amount + FRAG_COMMA_ORDER_ID + orderId);
                return true;
            }
            var body = new java.util.HashMap<String, Object>();
            body.put("orderId", orderId);
            body.put("amount", amount);
            Boolean result = restTemplate.postForObject("http://localhost:8084/payment/refund", body, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RestClientException ex) {
            log.error(MSG_PAYMENT_REFUND_CALL_FAILED_FOR_ORDER + orderId + FRAG_COMMA_AMOUNT + amount, ex);
            return false;
        }
    }

    @Override
    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public Order cancel(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() == OrderStatus.CANCELLED) return order;
        if (order.getStatus() == OrderStatus.REFUNDED) throw new IllegalStateException("Cannot cancel a refunded order");

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            // Compensating actions: refund payment and restore inventory
            log.info(MSG_CANCELLING_CONFIRMED_ORDER + id);
            boolean refunded = simulatePaymentRefund(order.getId(), order.getTotal());
            if (!refunded) {
                throw new IllegalStateException("Refund failed during cancellation");
            }
            try {
                boolean restored = inventoryClient.restore(order.getItems());
                if (!restored) {
                    log.warn(MSG_INV_RESTORE_REPORTED_FAILURE_FOR_ORDER + id);
                } else {
                    log.info(MSG_INV_RESTORE_SUCCEEDED_FOR_ORDER + id);
                }
            } catch (RestClientException ex) {
                log.error(MSG_INV_RESTORE_CALL_FAILED_FOR_ORDER + id, ex);
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order refund(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() == OrderStatus.REFUNDED) return order;
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED orders can be refunded");
        }
        boolean refunded = simulatePaymentRefund(order.getId(), order.getTotal());
        if (!refunded) {
            throw new IllegalStateException("Refund failed");
        }
        log.info(MSG_REFUNDING_ORDER + id + FRAG_COMMA_AMOUNT + order.getTotal());
        order.setStatus(OrderStatus.REFUNDED);
        return orderRepository.save(order);
    }
}
