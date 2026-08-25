package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute, String> {
    List<Dispute> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Dispute> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<Dispute> findAllByOrderByCreatedAtDesc();
    List<Dispute> findByOrderSellerIdOrderByCreatedAtDesc(String orderSellerId);
}
