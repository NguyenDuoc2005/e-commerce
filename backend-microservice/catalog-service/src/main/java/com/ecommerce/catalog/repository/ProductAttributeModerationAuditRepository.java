package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ProductAttributeModerationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeModerationAuditRepository extends JpaRepository<ProductAttributeModerationAudit, String> {
}
