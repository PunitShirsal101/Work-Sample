package com.order.logging;

/**
 * Centralized log message constants for order-service.
 */
public final class OrderLogMessages {
    private OrderLogMessages() {}

    // Log message constants
    public static final String MSG_PAYMENT_FAILED_FOR_ORDER = "Payment failed for orderId=";
    public static final String MSG_REFUND_FAILED_AFTER_DEDUCT_FAIL = "Compensating refund failed when inventory deduction failed, orderId=";
    public static final String MSG_REFUND_SUCCEEDED_AFTER_DEDUCT_FAIL = "Compensating refund succeeded after deduction failure, orderId=";
    public static final String MSG_PUBLISHED_ORDER_CREATED_EVENT = "Published OrderCreatedEvent for orderId=";
    public static final String MSG_EXTERNAL_CALL_FAILED_DURING_CREATE = "External call failed during order creation, orderId=";
    public static final String MSG_INV_RESTORE_FAIL_DURING_COMP = "Inventory restore reported failure during compensation, orderId=";
    public static final String MSG_INV_RESTORE_SUCCESS_DURING_COMP = "Inventory restore succeeded during compensation, orderId=";
    public static final String MSG_INV_RESTORE_CALL_FAILED_DURING_COMP = "Inventory restore call failed during compensation, orderId=";
    public static final String MSG_REFUND_FAILED_DURING_EXCEPTION = "Compensating refund failed during exception handling, orderId=";
    public static final String MSG_REFUND_SUCCEEDED_DURING_EXCEPTION = "Compensating refund succeeded during exception handling, orderId=";
    public static final String MSG_UNEXPECTED_ERROR_DURING_CREATE = "Unexpected error during order creation, orderId=";
    public static final String MSG_INV_RESTORE_FAIL_DURING_UNEXPECTED = "Inventory restore reported failure during unexpected error, orderId=";
    public static final String MSG_INV_RESTORE_SUCCESS_DURING_UNEXPECTED = "Inventory restore succeeded during unexpected error, orderId=";
    public static final String MSG_INV_RESTORE_CALL_FAILED_DURING_UNEXPECTED = "Inventory restore call failed during unexpected error, orderId=";
    public static final String MSG_REFUND_FAILED_DURING_UNEXPECTED = "Compensating refund failed during unexpected error, orderId=";
    public static final String MSG_REFUND_SUCCEEDED_DURING_UNEXPECTED = "Compensating refund succeeded during unexpected error, orderId=";
    public static final String MSG_SKIP_PAYMENT_CHARGE_NON_POSITIVE = "Skipping payment charge for non-positive amount: ";
    public static final String MSG_PAYMENT_SERVICE_CALL_FAILED_FOR_USER = "Payment service call failed for userId=";
    public static final String MSG_SKIP_PAYMENT_REFUND_NON_POSITIVE = "Skipping payment refund for non-positive amount: ";
    public static final String MSG_PAYMENT_REFUND_CALL_FAILED_FOR_ORDER = "Payment refund call failed for orderId=";
    public static final String MSG_CANCELLING_CONFIRMED_ORDER = "Cancelling confirmed order: issuing refund and requesting inventory restore, orderId=";
    public static final String MSG_INV_RESTORE_REPORTED_FAILURE_FOR_ORDER = "Inventory restore reported failure for orderId=";
    public static final String MSG_INV_RESTORE_SUCCEEDED_FOR_ORDER = "Inventory restore succeeded for orderId=";
    public static final String MSG_INV_RESTORE_CALL_FAILED_FOR_ORDER = "Inventory restore call failed for orderId=";
    public static final String MSG_REFUNDING_ORDER = "Refunding order, orderId=";

    // Common fragments
    public static final String FRAG_COMMA_ORDER_ID = ", orderId=";
    public static final String FRAG_COMMA_AMOUNT = ", amount=";
}
