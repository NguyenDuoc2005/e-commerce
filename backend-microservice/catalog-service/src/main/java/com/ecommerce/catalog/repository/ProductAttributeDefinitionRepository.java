package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeDefinitionRepository extends JpaRepository<ProductAttributeDefinition, String> {
}
