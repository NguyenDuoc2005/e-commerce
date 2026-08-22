package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.ShopFollow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopFollowRepository extends JpaRepository<ShopFollow, Long> {

    long countBySellerId(String sellerId);

    boolean existsBySellerIdAndCustomerId(String sellerId, String customerId);

    long deleteBySellerIdAndCustomerId(String sellerId, String customerId);
}
