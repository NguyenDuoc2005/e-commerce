package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.CommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionConfigRepository extends JpaRepository<CommissionConfig, String> {
    Optional<CommissionConfig> findFirstByCategoryIdAndActiveTrueOrderByCreatedAtDesc(String categoryId);
    Optional<CommissionConfig> findFirstByCategoryIdIsNullAndActiveTrueOrderByCreatedAtDesc();
}
