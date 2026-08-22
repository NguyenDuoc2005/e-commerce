package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.ecommerce.catalog.constant.EntityStatus;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, String> {
    List<ProductAttributeValue> findByProduct_IdOrderByDisplayOrderAsc(String productId);
    List<ProductAttributeValue> findByAttribute_Id(String attributeId);
    java.util.Optional<ProductAttributeValue> findByProduct_IdAndAttribute_Id(String productId, String attributeId);
    long countByAttribute_Id(String attributeId);
    List<ProductAttributeValue> findByProduct_Category_IdAndAttribute_IdAndStatus(
            String categoryId,
            String attributeId,
            EntityStatus status
    );
}
