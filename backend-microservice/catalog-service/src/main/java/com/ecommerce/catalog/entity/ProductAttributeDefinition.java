package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attribute_definition", indexes = {
        @Index(name = "idx_attribute_definition_name_status", columnList = "normalized_name, status"),
        @Index(name = "idx_attribute_definition_creator_verified_status", columnList = "created_by_seller_id, is_verified, status")
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

    @Column(name = "created_by_seller_id", length = 36)
    private String createdBySellerId;

    @Column(name = "default_unit", length = 50)
    private String defaultUnit;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "merged_into_definition_id", length = 36)
    private String mergedIntoDefinitionId;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public AttributeDataType getDataType() { return dataType; }
    public void setDataType(AttributeDataType dataType) { this.dataType = dataType; }
    public String getCreatedBySellerId() { return createdBySellerId; }
    public void setCreatedBySellerId(String createdBySellerId) { this.createdBySellerId = createdBySellerId; }
    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getMergedIntoDefinitionId() { return mergedIntoDefinitionId; }
    public void setMergedIntoDefinitionId(String mergedIntoDefinitionId) { this.mergedIntoDefinitionId = mergedIntoDefinitionId; }
}
