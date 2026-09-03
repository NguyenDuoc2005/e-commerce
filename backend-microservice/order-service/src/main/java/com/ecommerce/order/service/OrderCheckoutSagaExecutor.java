package com.ecommerce.order.service;

import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PromotionClient;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderSagaStep;
import com.ecommerce.order.entity.OrderSagaStepName;
import com.ecommerce.order.model.request.CheckoutProductItem;
import com.ecommerce.order.model.request.CheckoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderCheckoutSagaExecutor {
    private static final Logger log = LoggerFactory.getLogger(OrderCheckoutSagaExecutor.class);
    private static final int MAX_ATTEMPTS_PER_INVOCATION = 3;
    private static final int MAX_AUTOMATIC_ATTEMPTS = 15; // 3 attempts x 5 scheduler runs
    private static final long[] BACKOFF_MILLIS = {1000L, 3000L, 5000L};

    private final CatalogClient catalogClient;
    private final PromotionClient promotionClient;
    private final CartClient cartClient;
    private final OrderSagaStepPersistence persistence;
    private final OrderCheckoutDataService orderData;

    public OrderCheckoutSagaExecutor(CatalogClient catalogClient, PromotionClient promotionClient,
                                     CartClient cartClient, OrderSagaStepPersistence persistence,
                                     OrderCheckoutDataService orderData) {
        this.catalogClient = catalogClient;
        this.promotionClient = promotionClient;
        this.cartClient = cartClient;
        this.persistence = persistence;
        this.orderData = orderData;
    }

    public void execute(String orderId, CheckoutRequest request) {
        try {
            OrderSagaStep stock = persistence.pending(orderId, OrderSagaStepName.STOCK_DEDUCTED);
            try {
                for (CheckoutProductItem item : request.getProduct()) {
                    catalogClient.adjustStock(item.getId(), -safe(item.getQuantity()));
                }
                persistence.success(stock.getId());
            } catch (RuntimeException ex) {
                persistence.failed(stock.getId(), message(ex));
                compensateCompletedSteps(orderId);
                throw ex;
            }

            String voucherId = voucherIdForOrder(orderId);
            if (voucherId != null) {
                OrderSagaStep voucher = persistence.pending(orderId, OrderSagaStepName.VOUCHER_APPLIED);
                try {
                    promotionClient.decrementVoucher(voucherId);
                    persistence.success(voucher.getId());
                } catch (RuntimeException ex) {
                    persistence.failed(voucher.getId(), message(ex));
                    compensateCompletedSteps(orderId);
                    throw ex;
                }
            }

            if (request.getCustomer() != null && !request.getCustomer().isBlank()
                    && !isRetailCustomer(request.getCustomer())) {
                OrderSagaStep cart = persistence.pending(orderId, OrderSagaStepName.CART_CLEARED);
                try {
                    cartClient.deleteItems(request.getCustomer(), request.getProduct().stream()
                            .map(CheckoutProductItem::getId).toList());
                    persistence.success(cart.getId());
                } catch (RuntimeException ex) {
                    persistence.failed(cart.getId(), message(ex));
                    compensateCompletedSteps(orderId);
                    throw ex;
                }
            }
        } catch (RuntimeException ex) {
            // The caller owns the local transaction and will roll the order back.
            // Saga rows are REQUIRES_NEW so the failed/compensation state remains durable.
            throw ex;
        }
    }

    public void compensateCompletedSteps(String orderId) {
        for (OrderSagaStep step : persistence.successful(orderId)) {
            retryCompensation(step, false);
        }
    }

    public Map<String, Object> retryCompensationManually(String stepId) {
        OrderSagaStep step = persistence.get(stepId);
        if (step.getStatus() != com.ecommerce.order.entity.OrderSagaStepStatus.COMPENSATION_FAILED) {
            throw new IllegalArgumentException("SAGA_STEP_NOT_COMPENSATION_FAILED");
        }
        boolean recovered = retryCompensation(step, true);
        OrderSagaStep finalStep = persistence.get(stepId);
        return Map.of("id", finalStep.getId(), "orderId", finalStep.getOrderId(),
                "stepName", finalStep.getStepName(), "status", finalStep.getStatus(),
                "attemptCount", finalStep.getAttemptCount(), "recovered", recovered);
    }

    public void retryCompensationAutomatically(String stepId) {
        retryCompensation(persistence.get(stepId), false);
    }

    private boolean retryCompensation(OrderSagaStep step, boolean manual) {
        if (!manual && step.getAttemptCount() >= MAX_AUTOMATIC_ATTEMPTS) {
            log.error("Skipping automatic compensation after {} attempts: orderId={}, stepId={}",
                    step.getAttemptCount(), step.getOrderId(), step.getId());
            return false;
        }
        persistence.compensating(step.getId());
        int maxAttempts = manual ? MAX_ATTEMPTS_PER_INVOCATION
                : Math.min(MAX_ATTEMPTS_PER_INVOCATION, MAX_AUTOMATIC_ATTEMPTS - step.getAttemptCount());
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                compensate(step);
                persistence.compensated(step.getId());
                return true;
            } catch (RuntimeException ex) {
                persistence.compensationAttemptFailed(step.getId(), message(ex));
                if (attempt + 1 < maxAttempts) sleep(BACKOFF_MILLIS[attempt]);
            }
        }
        persistence.compensationFailed(step.getId(), "Compensation failed after retries: " + step.getStepName());
        log.error("CRITICAL checkout compensation failure: orderId={}, stepId={}, step={}",
                step.getOrderId(), step.getId(), step.getStepName());
        return false;
    }

    private void compensate(OrderSagaStep step) {
        switch (step.getStepName()) {
            case STOCK_DEDUCTED -> {
                for (OrderItem item : orderData.items(step.getOrderId())) {
                    catalogClient.adjustStock(item.getProductVariantId(), safe(item.getQuantity()));
                }
            }
            case VOUCHER_APPLIED -> {
                String voucherId = orderData.voucherId(step.getOrderId());
                if (voucherId != null) {
                    promotionClient.incrementVoucher(voucherId);
                }
            }
            case CART_CLEARED -> throw new UnsupportedOperationException("Cart clear has no reverse API");
        }
    }

    private String voucherIdForOrder(String orderId) {
        return orderData.voucherId(orderId);
    }

    private static int safe(Integer value) { return value == null ? 0 : value; }
    private static String message(Throwable ex) { return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(); }
    private static boolean isRetailCustomer(String customer) {
        return "khach le".equalsIgnoreCase(customer) || "khÃƒÂ¡ch lÃ¡ÂºÂ»".equals(customer);
    }
    private static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Compensation retry interrupted", interrupted);
        }
    }
}
