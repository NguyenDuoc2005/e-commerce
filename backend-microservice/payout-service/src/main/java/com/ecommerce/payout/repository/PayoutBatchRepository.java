package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.PayoutBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutBatchRepository extends JpaRepository<PayoutBatch, String> {
    List<PayoutBatch> findAllByOrderByCreatedAtDesc();
}
