package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    List<ProductImage> findByProduct_IdAndStatusOrderByDisplayOrderAsc(String productId, EntityStatus status);
    List<ProductImage> findByProduct_IdOrderByDisplayOrderAsc(String productId);
    void deleteByProduct_Id(String productId);
}
