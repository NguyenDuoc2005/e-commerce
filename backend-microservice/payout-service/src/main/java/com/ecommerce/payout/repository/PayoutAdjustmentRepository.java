package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.PayoutAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayoutAdjustmentRepository extends JpaRepository<PayoutAdjustment, String> {
    Optional<PayoutAdjustment> findByDisputeId(String disputeId);
}
