package com.ecommerce.order.service;

import com.ecommerce.order.entity.OrderSagaStep;
import com.ecommerce.order.entity.OrderSagaStepName;
import com.ecommerce.order.entity.OrderSagaStepStatus;
import com.ecommerce.order.repository.OrderSagaStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderSagaStepPersistence {
    private final OrderSagaStepRepository repository;

    public OrderSagaStepPersistence(OrderSagaStepRepository repository) { this.repository = repository; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderSagaStep pending(String orderId, OrderSagaStepName name) {
        OrderSagaStep step = new OrderSagaStep();
        step.setOrderId(orderId);
        step.setStepName(name);
        step.setStatus(OrderSagaStepStatus.PENDING);
        step.setAttemptCount(0);
        return repository.saveAndFlush(step);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(String id) { update(id, OrderSagaStepStatus.SUCCESS, null, false); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(String id, String error) { update(id, OrderSagaStepStatus.FAILED, error, true); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensating(String id) { update(id, OrderSagaStepStatus.COMPENSATING, null, false); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensationAttemptFailed(String id, String error) {
        update(id, OrderSagaStepStatus.COMPENSATING, error, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensated(String id) { update(id, OrderSagaStepStatus.COMPENSATED, null, false); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensationFailed(String id, String error) {
        update(id, OrderSagaStepStatus.COMPENSATION_FAILED, error, false);
    }

    @Transactional(readOnly = true)
    public List<OrderSagaStep> successful(String orderId) {
        return repository.findByOrderIdAndStatusOrderByCreatedAtDesc(orderId, OrderSagaStepStatus.SUCCESS);
    }

    @Transactional(readOnly = true)
    public List<OrderSagaStep> failedCompensations() {
        return repository.findByStatusOrderByCreatedAtAsc(OrderSagaStepStatus.COMPENSATION_FAILED);
    }

    @Transactional(readOnly = true)
    public OrderSagaStep get(String id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("SAGA_STEP_NOT_FOUND"));
    }

    private void update(String id, OrderSagaStepStatus status, String error, boolean incrementAttempt) {
        OrderSagaStep step = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Saga step not found: " + id));
        step.setStatus(status);
        if (incrementAttempt) step.setAttemptCount(step.getAttemptCount() + 1);
        step.setLastError(error);
        repository.save(step);
    }
}
