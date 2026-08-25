package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ProductAttributeModerationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttributeModerationAuditRepository extends JpaRepository<ProductAttributeModerationAudit, String> {
    List<ProductAttributeModerationAudit> findAllByOrderByCreatedDateDesc();
}
