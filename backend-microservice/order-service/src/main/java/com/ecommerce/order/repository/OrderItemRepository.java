package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByHoaDonId(String orderId);
}
