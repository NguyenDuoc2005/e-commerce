package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "checkout_idempotency_key", indexes = {
        @Index(name = "uk_checkout_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_checkout_idempotency_customer", columnList = "customer_id")
})
public class CheckoutIdempotencyKey {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "order_id", length = 36)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CheckoutIdempotencyStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public CheckoutIdempotencyStatus getStatus() { return status; }
    public void setStatus(CheckoutIdempotencyStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
