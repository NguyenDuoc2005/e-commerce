package com.ecommerce.payout.repository;

import com.ecommerce.payout.entity.SellerWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerWalletRepository extends JpaRepository<SellerWallet, String> {
    Optional<SellerWallet> findBySellerId(String sellerId);
}
