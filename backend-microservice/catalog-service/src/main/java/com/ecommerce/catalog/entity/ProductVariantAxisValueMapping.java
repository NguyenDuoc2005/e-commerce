package com.ecommerce.catalog.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_variant_axis_value_mapping")
public class ProductVariantAxisValueMapping {

    @EmbeddedId
    private ProductVariantAxisValueMappingId id;

    @MapsId("productVariantId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant variant;

    @MapsId("axisValueId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "axis_value_id", nullable = false)
    private ProductVariantAxisValue axisValue;

    public ProductVariantAxisValueMappingId getId() { return id; }
    public void setId(ProductVariantAxisValueMappingId id) { this.id = id; }
    public ProductVariant getVariant() { return variant; }
    public void setVariant(ProductVariant variant) { this.variant = variant; }
    public ProductVariantAxisValue getAxisValue() { return axisValue; }
    public void setAxisValue(ProductVariantAxisValue axisValue) { this.axisValue = axisValue; }
}
