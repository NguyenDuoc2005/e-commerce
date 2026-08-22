package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.SellerReceivable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerReceivableRepository extends JpaRepository<SellerReceivable, String> {
    Optional<SellerReceivable> findByOrderSellerId(String orderSellerId);
    List<SellerReceivable> findBySellerIdOrderByCreatedAtDesc(String sellerId);
}
