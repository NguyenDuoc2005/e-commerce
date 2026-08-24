package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "category_attribute_suggestion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_attribute_suggestion", columnNames = {"category_id", "attribute_definition_id"})
}, indexes = @Index(name = "idx_category_attribute_order", columnList = "category_id, display_order"))
public class CategoryAttributeSuggestion extends PrimaryEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private ProductAttributeDefinition definition;

    @Column(name = "filterable", nullable = false)
    private boolean filterable;

    @Column(name = "required_value", nullable = false)
    private boolean requiredValue;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public ProductAttributeDefinition getDefinition() { return definition; }
    public void setDefinition(ProductAttributeDefinition definition) { this.definition = definition; }
    public boolean isFilterable() { return filterable; }
    public void setFilterable(boolean filterable) { this.filterable = filterable; }
    public boolean isRequiredValue() { return requiredValue; }
    public void setRequiredValue(boolean requiredValue) { this.requiredValue = requiredValue; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
