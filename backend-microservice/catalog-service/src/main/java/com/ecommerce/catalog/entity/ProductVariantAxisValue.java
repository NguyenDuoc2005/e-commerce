package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "product_variant_axis_value", uniqueConstraints =
        @UniqueConstraint(name = "uk_axis_value_normalized", columnNames = {"axis_id", "normalized_value"}))
public class ProductVariantAxisValue extends PrimaryEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "axis_id", nullable = false)
    private ProductVariantAxis axis;

    @Column(nullable = false)
    private String value;

    @Column(name = "normalized_value", nullable = false)
    private String normalizedValue;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public ProductVariantAxis getAxis() { return axis; }
    public void setAxis(ProductVariantAxis axis) { this.axis = axis; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
