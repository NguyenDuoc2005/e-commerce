package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.SellerStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerStatusHistoryRepository extends JpaRepository<SellerStatusHistory, Long> {
}
