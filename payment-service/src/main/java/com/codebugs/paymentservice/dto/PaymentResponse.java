package com.codebugs.paymentservice.dto;

import com.codebugs.paymentservice.model.Payment;
import com.codebugs.paymentservice.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    UUID orderId,
    String customerId,
    BigDecimal amount,
    PaymentStatus status,
    String failureReason,
    Instant createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getCustomerId(),
            p.getAmount(), p.getStatus(), p.getFailureReason(), p.getCreatedAt());
    }
}
