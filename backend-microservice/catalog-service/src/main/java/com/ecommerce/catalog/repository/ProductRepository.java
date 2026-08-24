package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByStatusOrderByCreatedDateDesc(EntityStatus status);
    Page<Product> findBySellerIdAndNameContainingIgnoreCaseOrderByCreatedDateDesc(String sellerId, String q, Pageable pageable);
    Page<Product> findByStatusAndNameContainingIgnoreCaseOrderByCreatedDateDesc(EntityStatus status, String q, Pageable pageable);
    Page<Product> findBySellerIdAndStatusAndNameContainingIgnoreCaseOrderByCreatedDateDesc(String sellerId, EntityStatus status, String q, Pageable pageable);
    Optional<Product> findByIdAndSellerId(String id, String sellerId);
    List<Product> findByCategory_IdInAndStatus(List<String> categoryIds, EntityStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    Optional<Product> findLockedById(@Param("id") String id);
}
