package com.ecommerce.order.service;

import com.ecommerce.order.entity.CheckoutIdempotencyKey;
import com.ecommerce.order.entity.CheckoutIdempotencyStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class CheckoutIdempotencyCoordinator {
    private final CheckoutIdempotencyReservationService reservations;
    private final OrderCheckoutDataService orderData;

    public CheckoutIdempotencyCoordinator(CheckoutIdempotencyReservationService reservations, OrderCheckoutDataService orderData) {
        this.reservations = reservations;
        this.orderData = orderData;
    }

    public Object execute(String customerId, String key, Supplier<Object> action) {
        CheckoutIdempotencyKey reservation = reserve(customerId, key);
        if (reservation.getStatus() == CheckoutIdempotencyStatus.COMPLETED) {
            if (reservation.getOrderId() == null || reservation.getOrderId().isBlank()) {
                throw new IllegalStateException("Completed idempotency key has no order");
            }
            return orderData.checkoutResponse(reservation.getOrderId());
        }

        try {
            Object result = action.get();
            String orderId = extractOrderId(result);
            reservations.markCompleted(reservation.getId(), orderId);
            return result;
        } catch (RuntimeException exception) {
            reservations.markFailed(reservation.getId());
            throw exception;
        }
    }

    private CheckoutIdempotencyKey reserve(String customerId, String key) {
        try {
            return reservations.create(customerId, key);
        } catch (DataIntegrityViolationException duplicate) {
            Optional<CheckoutIdempotencyKey> existing = reservations.find(customerId, key);
            if (existing.isEmpty()) throw duplicate;
            CheckoutIdempotencyKey reservation = existing.get();
            if (reservation.getStatus() == CheckoutIdempotencyStatus.IN_PROGRESS) {
                throw new CheckoutIdempotencyConflictException("Checkout voi Idempotency-Key nay dang duoc xu ly");
            }
            if (reservation.getStatus() == CheckoutIdempotencyStatus.COMPLETED) return reservation;
            return reservations.retryFailed(reservation);
        }
    }

    private String extractOrderId(Object result) {
        if (result instanceof Map<?, ?> map && map.get("id") != null) {
            return String.valueOf(map.get("id"));
        }
        throw new IllegalStateException("Checkout completed without order id");
    }
}
