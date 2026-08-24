package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variant", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_variant_sku", columnNames = "sku"),
        @UniqueConstraint(name = "uk_product_variant_combination", columnNames = {"product_id", "combination_key"})
})
@DynamicUpdate
public class ProductVariant extends PrimaryEntity implements Serializable {

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "combination_key", nullable = false, length = 255)
    private String combinationKey;

    @Column(name = "sale_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(name = "is_default", nullable = false)
    private boolean defaultVariant;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getCombinationKey() { return combinationKey; }
    public void setCombinationKey(String combinationKey) { this.combinationKey = combinationKey; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public boolean isDefaultVariant() { return defaultVariant; }
    public void setDefaultVariant(boolean defaultVariant) { this.defaultVariant = defaultVariant; }
}
