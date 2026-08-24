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
@Table(name = "product_attribute_option", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attribute_option_normalized", columnNames = {"attribute_definition_id", "normalized_value"})
}, indexes = @Index(name = "idx_attribute_option_attribute", columnList = "attribute_definition_id"))
public class ProductAttributeOption extends PrimaryEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private ProductAttributeDefinition definition;

    @Column(nullable = false, length = 500)
    private String value;

    @Column(name = "normalized_value", nullable = false, length = 500)
    private String normalizedValue;

    @Column(name = "created_by_seller_id", length = 36)
    private String createdBySellerId;

    @Column(name = "merged_into_option_id", length = 36)
    private String mergedIntoOptionId;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public ProductAttributeDefinition getDefinition() { return definition; }
    public void setDefinition(ProductAttributeDefinition definition) { this.definition = definition; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }
    public String getCreatedBySellerId() { return createdBySellerId; }
    public void setCreatedBySellerId(String createdBySellerId) { this.createdBySellerId = createdBySellerId; }
    public String getMergedIntoOptionId() { return mergedIntoOptionId; }
    public void setMergedIntoOptionId(String mergedIntoOptionId) { this.mergedIntoOptionId = mergedIntoOptionId; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
