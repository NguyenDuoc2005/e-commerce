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

import java.math.BigDecimal;

@Entity
@Table(name = "product_attribute_value", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_attribute_value", columnNames = {"product_id", "attribute_id"})
}, indexes = @Index(name = "idx_product_attribute_product", columnList = "product_id, display_order"))
public class ProductAttributeValue extends PrimaryEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private ProductAttributeDefinition attribute;

    @Column(name = "text_value", columnDefinition = "text")
    private String textValue;

    @Column(name = "number_value", precision = 19, scale = 4)
    private BigDecimal numberValue;

    @Column(length = 50)
    private String unit;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public ProductAttributeDefinition getAttribute() { return attribute; }
    public void setAttribute(ProductAttributeDefinition attribute) { this.attribute = attribute; }
    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }
    public BigDecimal getNumberValue() { return numberValue; }
    public void setNumberValue(BigDecimal numberValue) { this.numberValue = numberValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
