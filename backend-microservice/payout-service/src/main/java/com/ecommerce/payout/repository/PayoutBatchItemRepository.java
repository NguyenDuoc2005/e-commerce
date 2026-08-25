package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.PayoutBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutBatchItemRepository extends JpaRepository<PayoutBatchItem, String> {
}
