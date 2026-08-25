package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.SellerWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface SellerWalletRepository extends JpaRepository<SellerWallet, String> {
    Optional<SellerWallet> findBySellerId(String sellerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM SellerWallet w WHERE w.sellerId = :sellerId")
    Optional<SellerWallet> findBySellerIdForUpdate(@Param("sellerId") String sellerId);
}
