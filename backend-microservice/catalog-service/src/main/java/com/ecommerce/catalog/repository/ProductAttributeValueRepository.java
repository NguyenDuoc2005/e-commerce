package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.ecommerce.catalog.constant.EntityStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, String> {
    List<ProductAttributeValue> findByProduct_IdOrderByDisplayOrderAsc(String productId);
    List<ProductAttributeValue> findByDefinition_Id(String definitionId);
    long countByDefinition_Id(String definitionId);
    List<ProductAttributeValue> findByProduct_Category_IdAndDefinition_IdAndStatus(
            String categoryId,
            String definitionId,
            EntityStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select value from ProductAttributeValue value where value.product.id = :productId and value.definition.id = :definitionId")
    List<ProductAttributeValue> findLockedByProductAndDefinition(
            @Param("productId") String productId,
            @Param("definitionId") String definitionId);

    @Modifying
    void deleteByProduct_Id(String productId);
}
