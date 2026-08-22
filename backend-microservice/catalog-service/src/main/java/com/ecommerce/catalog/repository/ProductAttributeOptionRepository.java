package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeOptionRepository extends JpaRepository<ProductAttributeOption, String> {
    List<ProductAttributeOption> findByAttribute_IdAndStatusOrderByDisplayOrderAsc(String attributeId, EntityStatus status);
    Optional<ProductAttributeOption> findByAttribute_IdAndNormalizedValueAndStatus(String attributeId, String normalizedValue, EntityStatus status);
}
