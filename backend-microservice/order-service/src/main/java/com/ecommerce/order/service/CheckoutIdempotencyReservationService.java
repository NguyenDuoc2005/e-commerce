package com.ecommerce.order.service;

import com.ecommerce.order.entity.CheckoutIdempotencyKey;
import com.ecommerce.order.entity.CheckoutIdempotencyStatus;
import com.ecommerce.order.repository.CheckoutIdempotencyKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CheckoutIdempotencyReservationService {
    private final CheckoutIdempotencyKeyRepository repository;

    public CheckoutIdempotencyReservationService(CheckoutIdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CheckoutIdempotencyKey create(String customerId, String key) {
        CheckoutIdempotencyKey reservation = new CheckoutIdempotencyKey();
        reservation.setCustomerId(customerId);
        reservation.setIdempotencyKey(key);
        reservation.setStatus(CheckoutIdempotencyStatus.IN_PROGRESS);
        return repository.saveAndFlush(reservation);
    }

    @Transactional(readOnly = true)
    public Optional<CheckoutIdempotencyKey> find(String customerId, String key) {
        return repository.findByCustomerIdAndIdempotencyKey(customerId, key);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String id, String orderId) {
        CheckoutIdempotencyKey reservation = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Idempotency reservation not found: " + id));
        reservation.setOrderId(orderId);
        reservation.setStatus(CheckoutIdempotencyStatus.COMPLETED);
        repository.save(reservation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String id) {
        repository.findById(id).ifPresent(reservation -> {
            reservation.setStatus(CheckoutIdempotencyStatus.FAILED);
            repository.save(reservation);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CheckoutIdempotencyKey retryFailed(CheckoutIdempotencyKey existing) {
        existing.setStatus(CheckoutIdempotencyStatus.IN_PROGRESS);
        existing.setOrderId(null);
        return repository.saveAndFlush(existing);
    }
}
