package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeDefinitionRepository extends JpaRepository<ProductAttributeDefinition, String> {
    List<ProductAttributeDefinition> findByNormalizedNameContainingIgnoreCaseAndStatusOrderByVerifiedDescNameAsc(
            String normalizedName, EntityStatus status);
    Optional<ProductAttributeDefinition> findByNormalizedNameAndStatus(String normalizedName, EntityStatus status);
    List<ProductAttributeDefinition> findByStatusOrderByVerifiedDescNameAsc(EntityStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductAttributeDefinition> findLockedById(String id);
}
