package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeOptionRepository extends JpaRepository<ProductAttributeOption, String> {
    List<ProductAttributeOption> findByDefinition_IdAndStatusOrderByDisplayOrderAsc(String definitionId, EntityStatus status);
    Optional<ProductAttributeOption> findByDefinition_IdAndNormalizedValueAndStatus(String definitionId, String normalizedValue, EntityStatus status);
}
