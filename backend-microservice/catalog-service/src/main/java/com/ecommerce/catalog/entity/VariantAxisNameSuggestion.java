package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "variant_axis_name_suggestion")
public class VariantAxisNameSuggestion extends PrimaryEntity {

    @Column(name = "merged_into_suggestion_id", length = 36)
    private String mergedIntoSuggestionId;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false, unique = true)
    private String normalizedName;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    public String getMergedIntoSuggestionId() { return mergedIntoSuggestionId; }
    public void setMergedIntoSuggestionId(String mergedIntoSuggestionId) { this.mergedIntoSuggestionId = mergedIntoSuggestionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
