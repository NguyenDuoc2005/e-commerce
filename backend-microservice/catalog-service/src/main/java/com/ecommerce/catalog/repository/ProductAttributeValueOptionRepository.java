package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ProductAttributeValueOption;
import com.ecommerce.catalog.entity.ProductAttributeValueOptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductAttributeValueOptionRepository extends JpaRepository<ProductAttributeValueOption, ProductAttributeValueOptionId> {
    List<ProductAttributeValueOption> findByProductAttributeValue_Id(String valueId);

    @Modifying
    @Query("delete from ProductAttributeValueOption selected where selected.productAttributeValue.id in :valueIds")
    void deleteByValueIds(@Param("valueIds") Collection<String> valueIds);
}
