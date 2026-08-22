package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryAttributeSuggestionRepository extends JpaRepository<CategoryAttributeSuggestion, String> {
    List<CategoryAttributeSuggestion> findByCategory_IdAndStatusOrderByDisplayOrderAsc(String categoryId, EntityStatus status);
    boolean existsByCategory_IdAndAttribute_IdAndStatus(String categoryId, String attributeId, EntityStatus status);
    List<CategoryAttributeSuggestion> findByAttribute_IdOrderByDisplayOrderAsc(String attributeId);
    Optional<CategoryAttributeSuggestion> findByCategory_IdAndAttribute_Id(String categoryId, String attributeId);
}
