package com.ecommerce.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductVariantAxisValueMappingId implements Serializable {

    @Column(name = "product_variant_id", length = 36)
    private String productVariantId;

    @Column(name = "axis_value_id", length = 36)
    private String axisValueId;

    public ProductVariantAxisValueMappingId() {}

    public ProductVariantAxisValueMappingId(String productVariantId, String axisValueId) {
        this.productVariantId = productVariantId;
        this.axisValueId = axisValueId;
    }

    public String getProductVariantId() { return productVariantId; }
    public String getAxisValueId() { return axisValueId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProductVariantAxisValueMappingId that)) return false;
        return Objects.equals(productVariantId, that.productVariantId) && Objects.equals(axisValueId, that.axisValueId);
    }

    @Override
    public int hashCode() { return Objects.hash(productVariantId, axisValueId); }
}
