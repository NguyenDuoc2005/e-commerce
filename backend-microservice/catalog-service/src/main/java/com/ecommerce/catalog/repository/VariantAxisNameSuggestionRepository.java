package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.VariantAxisNameSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VariantAxisNameSuggestionRepository extends JpaRepository<VariantAxisNameSuggestion, String> {
    List<VariantAxisNameSuggestion> findByNormalizedNameContainingIgnoreCaseAndStatusOrderByVerifiedDescNameAsc(String q, EntityStatus status);
    Optional<VariantAxisNameSuggestion> findByNormalizedName(String normalizedName);
}
