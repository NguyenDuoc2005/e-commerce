package com.ecommerce.order.service;

import com.ecommerce.order.entity.OrderSagaStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaReconciliationJob {
    private static final Logger log = LoggerFactory.getLogger(OrderSagaReconciliationJob.class);
    private final OrderSagaStepPersistence persistence;
    private final OrderCheckoutSagaExecutor executor;

    public OrderSagaReconciliationJob(OrderSagaStepPersistence persistence, OrderCheckoutSagaExecutor executor) {
        this.persistence = persistence;
        this.executor = executor;
    }

    @Scheduled(fixedDelayString = "${checkout.saga.reconciliation-delay-ms:900000}")
    public void reconcileCompensationFailures() {
        for (OrderSagaStep step : persistence.failedCompensations()) {
            try {
                executor.retryCompensationAutomatically(step.getId());
            } catch (RuntimeException exception) {
                log.error("Saga reconciliation failed: orderId={}, stepId={}", step.getOrderId(), step.getId(), exception);
            }
        }
    }
}
