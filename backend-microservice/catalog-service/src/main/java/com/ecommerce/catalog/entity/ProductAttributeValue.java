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
        @UniqueConstraint(name = "uk_product_attribute_value_slot", columnNames = {"product_id", "attribute_definition_id", "value_slot"})
}, indexes = @Index(name = "idx_product_attribute_product", columnList = "product_id, display_order"))
public class ProductAttributeValue extends PrimaryEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private ProductAttributeDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_option_id")
    private ProductAttributeOption option;

    @Column(name = "value_text", columnDefinition = "text")
    private String valueText;

    @Column(name = "value_number", precision = 19, scale = 4)
    private BigDecimal valueNumber;

    @Column(length = 50)
    private String unit;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "value_slot", insertable = false, updatable = false, length = 36)
    private String valueSlot;

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public ProductAttributeDefinition getDefinition() { return definition; }
    public void setDefinition(ProductAttributeDefinition definition) { this.definition = definition; }
    public ProductAttributeOption getOption() { return option; }
    public void setOption(ProductAttributeOption option) { this.option = option; }
    public String getValueText() { return valueText; }
    public void setValueText(String valueText) { this.valueText = valueText; }
    public BigDecimal getValueNumber() { return valueNumber; }
    public void setValueNumber(BigDecimal valueNumber) { this.valueNumber = valueNumber; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public String getValueSlot() { return valueSlot; }
}
