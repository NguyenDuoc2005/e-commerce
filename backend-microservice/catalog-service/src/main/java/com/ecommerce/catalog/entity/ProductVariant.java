package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Random;

@Entity
@Table(name = "product_variant")
@DynamicUpdate
public class ProductVariant extends PrimaryEntity implements Serializable {

    @Column(name = "code")
    private String code;

    @Column(name = "sale_price")
    private Double salePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "seller_id", length = 36)
    private String sellerId;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "size_id", referencedColumnName = "id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "color_id", referencedColumnName = "id")
    private Color color;

    @PrePersist
    void generateCode() {
        if (code == null || code.isBlank()) {
            code = String.format("SPCT%04d", new Random().nextInt(10000));
        }
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
}
