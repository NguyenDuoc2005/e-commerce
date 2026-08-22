package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderSeller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderSellerRepository extends JpaRepository<OrderSeller, String> {
    List<OrderSeller> findByOrderId(String orderId);
    List<OrderSeller> findBySellerIdOrderByCreatedDateDesc(String sellerId);
}
