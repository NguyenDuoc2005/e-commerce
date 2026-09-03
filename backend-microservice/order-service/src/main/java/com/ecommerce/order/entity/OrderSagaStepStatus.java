package com.ecommerce.order.entity;

public enum OrderSagaStepStatus {
    PENDING,
    SUCCESS,
    FAILED,
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED
}
