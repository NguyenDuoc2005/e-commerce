package com.ecommerce.catalog.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProductAttributeValueOptionId implements Serializable {
    private String productAttributeValue;
    private String option;

    public ProductAttributeValueOptionId() {
    }

    public ProductAttributeValueOptionId(String productAttributeValue, String option) {
        this.productAttributeValue = productAttributeValue;
        this.option = option;
    }

    public String getProductAttributeValue() { return productAttributeValue; }
    public void setProductAttributeValue(String productAttributeValue) { this.productAttributeValue = productAttributeValue; }
    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProductAttributeValueOptionId that)) return false;
        return Objects.equals(productAttributeValue, that.productAttributeValue) && Objects.equals(option, that.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productAttributeValue, option);
    }
}
