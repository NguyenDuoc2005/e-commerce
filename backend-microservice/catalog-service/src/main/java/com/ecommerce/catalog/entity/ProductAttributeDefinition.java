package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.AttributeNormalizationStatus;
import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attribute_definition", indexes = {
        @Index(name = "idx_attribute_normalized_name", columnList = "normalized_name"),
        @Index(name = "idx_attribute_creator_status", columnList = "creator_seller_id, normalization_status")
})
public class ProductAttributeDefinition extends PrimaryEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 30)
    private AttributeDataType dataType;

    @Column(name = "creator_seller_id", length = 36)
    private String creatorSellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "normalization_status", nullable = false, length = 30)
    private AttributeNormalizationStatus normalizationStatus;

    @Column(name = "merged_into_attribute_id", length = 36)
    private String mergedIntoAttributeId;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public AttributeDataType getDataType() { return dataType; }
    public void setDataType(AttributeDataType dataType) { this.dataType = dataType; }
    public String getCreatorSellerId() { return creatorSellerId; }
    public void setCreatorSellerId(String creatorSellerId) { this.creatorSellerId = creatorSellerId; }
    public AttributeNormalizationStatus getNormalizationStatus() { return normalizationStatus; }
    public void setNormalizationStatus(AttributeNormalizationStatus normalizationStatus) { this.normalizationStatus = normalizationStatus; }
    public String getMergedIntoAttributeId() { return mergedIntoAttributeId; }
    public void setMergedIntoAttributeId(String mergedIntoAttributeId) { this.mergedIntoAttributeId = mergedIntoAttributeId; }
}
