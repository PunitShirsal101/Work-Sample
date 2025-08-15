package com.order.api;

import com.order.api.dto.OrderCreateRequest;
import com.order.domain.Order;
import com.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderCreateRequest request,
                                        @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        Order order = orderService.create(request, idempotencyKey);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable String id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancel(@PathVariable String id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Order> refund(@PathVariable String id) {
        return ResponseEntity.ok(orderService.refund(id));
    }
}
