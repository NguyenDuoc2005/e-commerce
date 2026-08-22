package com.ecommerce.catalog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attribute_value_option")
@IdClass(ProductAttributeValueOptionId.class)
public class ProductAttributeValueOption {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_attribute_value_id", nullable = false)
    private ProductAttributeValue productAttributeValue;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductAttributeOption option;

    public ProductAttributeValue getProductAttributeValue() { return productAttributeValue; }
    public void setProductAttributeValue(ProductAttributeValue productAttributeValue) { this.productAttributeValue = productAttributeValue; }
    public ProductAttributeOption getOption() { return option; }
    public void setOption(ProductAttributeOption option) { this.option = option; }
}
