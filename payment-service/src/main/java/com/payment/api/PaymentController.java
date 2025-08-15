package com.payment.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @PostMapping("/charge")
    public ResponseEntity<Boolean> charge(@RequestBody PaymentChargeRequest request) {
        // Placeholder: always successful charge
        return ResponseEntity.ok(true);
    }

    @PostMapping("/refund")
    public ResponseEntity<Boolean> refund(@RequestBody PaymentRefundRequest request) {
        // Placeholder: always successful refund
        return ResponseEntity.ok(true);
    }

    public static class PaymentChargeRequest {
        private String userId;
        private BigDecimal amount;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class PaymentRefundRequest {
        private String orderId;
        private BigDecimal amount;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
