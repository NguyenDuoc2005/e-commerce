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
@Table(name = "product_variant_axis", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_axis_order", columnNames = {"product_id", "display_order"}),
        @UniqueConstraint(name = "uk_product_axis_name", columnNames = {"product_id", "normalized_name"})
})
public class ProductVariantAxis extends PrimaryEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_suggestion_id")
    private VariantAxisNameSuggestion nameSuggestion;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public VariantAxisNameSuggestion getNameSuggestion() { return nameSuggestion; }
    public void setNameSuggestion(VariantAxisNameSuggestion nameSuggestion) { this.nameSuggestion = nameSuggestion; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
