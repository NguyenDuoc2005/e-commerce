package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductVariantAxis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantAxisRepository extends JpaRepository<ProductVariantAxis, String> {
    List<ProductVariantAxis> findByProduct_IdAndStatusOrderByDisplayOrderAsc(String productId, EntityStatus status);
    List<ProductVariantAxis> findByProduct_IdOrderByDisplayOrderAsc(String productId);
    void deleteByProduct_Id(String productId);
}
