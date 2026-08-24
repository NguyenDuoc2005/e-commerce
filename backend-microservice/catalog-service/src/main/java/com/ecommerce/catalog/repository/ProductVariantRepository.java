package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProduct_IdOrderByCreatedDateDesc(String productId);
    List<ProductVariant> findByProduct_IdAndStatusOrderByCreatedDateDesc(String productId, EntityStatus status);
    List<ProductVariant> findByIdIn(List<String> ids);
    boolean existsBySku(String sku);
    boolean existsByProduct_IdAndCombinationKey(String productId, String combinationKey);
    void deleteByProduct_Id(String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select variant from ProductVariant variant join fetch variant.product where variant.id = :id")
    Optional<ProductVariant> findLockedById(@Param("id") String id);
}
