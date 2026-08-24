package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductVariantAxisValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductVariantAxisValueRepository extends JpaRepository<ProductVariantAxisValue, String> {
    List<ProductVariantAxisValue> findByAxis_IdAndStatusOrderByDisplayOrderAsc(String axisId, EntityStatus status);
    List<ProductVariantAxisValue> findByAxis_IdIn(Collection<String> axisIds);
    void deleteByAxis_IdIn(Collection<String> axisIds);
}
