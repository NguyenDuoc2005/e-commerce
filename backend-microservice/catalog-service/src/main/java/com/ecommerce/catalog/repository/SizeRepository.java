package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Size;
import com.ecommerce.catalog.constant.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SizeRepository extends JpaRepository<Size, String> {
    List<Size> findByNameContaining(String name);
    List<Size> findByStatusOrderByCreatedDateDesc(EntityStatus status);
}
