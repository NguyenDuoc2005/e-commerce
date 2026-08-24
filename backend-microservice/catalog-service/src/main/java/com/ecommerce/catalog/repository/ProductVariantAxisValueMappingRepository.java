package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ProductVariantAxisValueMapping;
import com.ecommerce.catalog.entity.ProductVariantAxisValueMappingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductVariantAxisValueMappingRepository extends JpaRepository<ProductVariantAxisValueMapping, ProductVariantAxisValueMappingId> {
    List<ProductVariantAxisValueMapping> findByVariant_Id(String variantId);
    List<ProductVariantAxisValueMapping> findByVariant_IdIn(Collection<String> variantIds);
    void deleteByVariant_IdIn(Collection<String> variantIds);
}
