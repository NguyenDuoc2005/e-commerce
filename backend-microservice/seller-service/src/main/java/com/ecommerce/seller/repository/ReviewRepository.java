package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {
    boolean existsByCustomerIdAndOrderSellerIdAndProductDetailId(String customerId, String orderSellerId, String productDetailId);
    List<Review> findByProductIdAndStatusOrderByCreatedAtDesc(String productId, String status);
    List<Review> findBySellerIdAndStatusOrderByCreatedAtDesc(String sellerId, String status);
    List<Review> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<Review> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    Optional<Review> findByIdAndSellerId(String id, String sellerId);
    long countByProductIdAndStatus(String productId, String status);
    long countBySellerIdAndStatus(String sellerId, String status);

    @Query("select avg(r.productRating) from Review r where r.productId = :productId and r.status = 'VISIBLE'")
    Double averageProductRating(@Param("productId") String productId);

    @Query("select avg(r.shopRating) from Review r where r.sellerId = :sellerId and r.status = 'VISIBLE'")
    Double averageShopRating(@Param("sellerId") String sellerId);
}
