package com.ecommerce.order.service;

import com.ecommerce.order.entity.CheckoutIdempotencyKey;
import com.ecommerce.order.entity.CheckoutIdempotencyStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutIdempotencyCoordinatorTest {
    @Mock private CheckoutIdempotencyReservationService reservations;
    @Mock private OrderCheckoutDataService orderData;

    @Test
    void completedKeyReturnsOriginalOrderWithoutRunningCheckoutAgain() {
        CheckoutIdempotencyKey existing = reservation(CheckoutIdempotencyStatus.COMPLETED, "order-1");
        when(reservations.create("customer-1", "key-1")).thenReturn(existing);
        when(orderData.checkoutResponse("order-1")).thenReturn(Map.of("id", "order-1"));
        AtomicInteger executions = new AtomicInteger();

        Object result = new CheckoutIdempotencyCoordinator(reservations, orderData)
                .execute("customer-1", "key-1", () -> { executions.incrementAndGet(); return Map.of("id", "new-order"); });

        assertEquals("order-1", ((Map<?, ?>) result).get("id"));
        assertEquals(0, executions.get());
        verify(reservations, never()).markCompleted(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void duplicateInProgressKeyReturnsConflict() {
        CheckoutIdempotencyKey existing = reservation(CheckoutIdempotencyStatus.IN_PROGRESS, null);
        when(reservations.create("customer-1", "key-1"))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(reservations.find("customer-1", "key-1")).thenReturn(Optional.of(existing));

        assertThrows(CheckoutIdempotencyConflictException.class, () ->
                new CheckoutIdempotencyCoordinator(reservations, orderData)
                        .execute("customer-1", "key-1", () -> Map.of("id", "order-1")));
    }

    @Test
    void concurrentSameKeyAllowsOneRequestAndRejectsTheOther() throws Exception {
        CheckoutIdempotencyKey inProgress = reservation(CheckoutIdempotencyStatus.IN_PROGRESS, null);
        AtomicInteger reservationsCreated = new AtomicInteger();
        when(reservations.create("customer-1", "key-1")).thenAnswer(invocation -> {
            if (reservationsCreated.getAndIncrement() == 0) return inProgress;
            throw new DataIntegrityViolationException("duplicate");
        });
        when(reservations.find("customer-1", "key-1")).thenReturn(Optional.of(inProgress));
        CountDownLatch firstRequestEntered = new CountDownLatch(1);
        CountDownLatch releaseFirstRequest = new CountDownLatch(1);
        CheckoutIdempotencyCoordinator coordinator = new CheckoutIdempotencyCoordinator(reservations, orderData);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Object> first = pool.submit(() -> coordinator.execute("customer-1", "key-1", () -> {
                firstRequestEntered.countDown();
                try {
                    releaseFirstRequest.await();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                }
                return Map.of("id", "order-1");
            }));
            firstRequestEntered.await();
            Future<Object> second = pool.submit(() -> coordinator.execute("customer-1", "key-1", () -> Map.of("id", "order-2")));
            ExecutionException error = org.junit.jupiter.api.Assertions.assertThrows(ExecutionException.class, second::get);
            assertEquals(CheckoutIdempotencyConflictException.class, error.getCause().getClass());
            releaseFirstRequest.countDown();
            assertEquals("order-1", ((Map<?, ?>) first.get()).get("id"));
        } finally {
            releaseFirstRequest.countDown();
            pool.shutdownNow();
        }
    }

    private static CheckoutIdempotencyKey reservation(CheckoutIdempotencyStatus status, String orderId) {
        CheckoutIdempotencyKey key = new CheckoutIdempotencyKey();
        key.setId("reservation-1");
        key.setStatus(status);
        key.setOrderId(orderId);
        return key;
    }
}
