package com.ecommerce.order.repository;

import com.ecommerce.order.entity.CheckoutIdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckoutIdempotencyKeyRepository extends JpaRepository<CheckoutIdempotencyKey, String> {
    Optional<CheckoutIdempotencyKey> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);
    Optional<CheckoutIdempotencyKey> findByIdempotencyKey(String idempotencyKey);
}
