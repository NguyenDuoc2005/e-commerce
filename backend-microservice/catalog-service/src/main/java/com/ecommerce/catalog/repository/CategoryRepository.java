package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByParentIsNullAndStatusOrderByDisplayOrderAsc(EntityStatus status);
    List<Category> findByParent_IdAndStatusOrderByDisplayOrderAsc(String parentId, EntityStatus status);
    boolean existsByParent_IdAndStatus(String parentId, EntityStatus status);
    Optional<Category> findByIdAndStatus(String id, EntityStatus status);
}
