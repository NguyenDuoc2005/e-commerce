package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryAttributeSuggestionRepository extends JpaRepository<CategoryAttributeSuggestion, String> {
    List<CategoryAttributeSuggestion> findByCategory_IdAndStatusOrderByDisplayOrderAsc(String categoryId, EntityStatus status);
    List<CategoryAttributeSuggestion> findByDefinition_IdOrderByDisplayOrderAsc(String definitionId);
    Optional<CategoryAttributeSuggestion> findByCategory_IdAndDefinition_Id(String categoryId, String definitionId);
    void deleteByCategory_Id(String categoryId);
}
