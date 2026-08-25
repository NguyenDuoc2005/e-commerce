package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.SellerReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface SellerReceivableRepository extends JpaRepository<SellerReceivable, String> {
    Optional<SellerReceivable> findByOrderSellerId(String orderSellerId);
    List<SellerReceivable> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM SellerReceivable r
            WHERE r.status = :status
              AND ((r.availableAt IS NOT NULL AND r.availableAt <= :now)
                   OR (r.availableAt IS NULL AND r.createdAt <= :legacyCreatedBefore))
            ORDER BY r.availableAt ASC, r.createdAt ASC
            """)
    List<SellerReceivable> findEligibleForRelease(@Param("status") String status,
                                                  @Param("now") Instant now,
                                                  @Param("legacyCreatedBefore") Instant legacyCreatedBefore);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM SellerReceivable r WHERE r.id IN :ids")
    List<SellerReceivable> findAllByIdForUpdate(@Param("ids") List<String> ids);
}
