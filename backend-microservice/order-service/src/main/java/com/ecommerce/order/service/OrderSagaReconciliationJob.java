package com.ecommerce.order.service;

import com.ecommerce.order.entity.OrderSagaStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class OrderSagaReconciliationJob {
    private static final Logger log = LoggerFactory.getLogger(OrderSagaReconciliationJob.class);
    private final OrderSagaStepPersistence persistence;
    private final OrderCheckoutSagaExecutor executor;
    private final MeterRegistry meters;

    public OrderSagaReconciliationJob(OrderSagaStepPersistence persistence, OrderCheckoutSagaExecutor executor) {
        this(persistence, executor, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());
    }
    @Autowired public OrderSagaReconciliationJob(OrderSagaStepPersistence persistence, OrderCheckoutSagaExecutor executor, MeterRegistry meters) {
        this.persistence = persistence;
        this.executor = executor;
        this.meters = meters;
    }

    @Scheduled(fixedDelayString = "${checkout.saga.reconciliation-delay-ms:900000}")
    public void reconcileCompensationFailures() {
        for (OrderSagaStep step : persistence.failedCompensations()) {
            meters.counter("order.saga.reconciliation.attempt","step",step.getStepName().name()).increment();
            try {
                executor.retryCompensationAutomatically(step.getId());
                meters.counter("order.saga.reconciliation.success","step",step.getStepName().name()).increment();
            } catch (RuntimeException exception) {
                meters.counter("order.saga.reconciliation.failure","step",step.getStepName().name()).increment();
                log.error("Saga reconciliation failed: orderId={}, stepId={}", step.getOrderId(), step.getId(), exception);
            }
        }
    }
}
