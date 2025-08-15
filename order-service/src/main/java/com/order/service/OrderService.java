package com.order.service;

import com.order.domain.Order;
import com.order.api.dto.OrderCreateRequest;

import java.util.Optional;

public interface OrderService {
    Order create(OrderCreateRequest request, String idempotencyKey);
    Optional<Order> findById(String id);
    Order cancel(String id);
    Order refund(String id);
}
