package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderSellerRepository extends JpaRepository<OrderSeller, String> {
    List<OrderSeller> findByOrderId(String orderId);
    List<OrderSeller> findBySellerIdOrderByCreatedDateDesc(String sellerId);

    @Query(value = """
            SELECT os.seller_id AS sellerId, COALESCE(SUM(oi.quantity), 0) AS soldCount
            FROM order_seller os
            JOIN order_item oi ON oi.order_seller_id = os.id
            WHERE os.order_status = :completedStatus AND os.seller_id IN (:sellerIds)
            GROUP BY os.seller_id
            """, nativeQuery = true)
    List<SellerSoldCount> findSoldCounts(
            @Param("sellerIds") List<String> sellerIds,
            @Param("completedStatus") int completedStatus);

    interface SellerSoldCount {
        String getSellerId();
        Long getSoldCount();
    }
}
