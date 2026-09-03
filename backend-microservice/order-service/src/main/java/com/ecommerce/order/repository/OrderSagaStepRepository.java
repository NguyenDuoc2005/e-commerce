package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderSagaStep;
import com.ecommerce.order.entity.OrderSagaStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderSagaStepRepository extends JpaRepository<OrderSagaStep, String> {
    List<OrderSagaStep> findByOrderIdAndStatusOrderByCreatedAtDesc(String orderId, OrderSagaStepStatus status);
    List<OrderSagaStep> findByStatusOrderByCreatedAtAsc(OrderSagaStepStatus status);
}
