package com.ecommerce.cart.entity;

import com.ecommerce.cart.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_detail")
public class CartDetail extends PrimaryEntity {
    @Column(name = "product_variant_id")
    private String productVariantId;

    @ManyToOne
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    private Cart cart;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Column(name = "seller_id", length = 36)
    private String sellerId;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "seller_slug")
    private String sellerSlug;

    public String getProductVariantId() { return productVariantId; }
    public void setProductVariantId(String productVariantId) { this.productVariantId = productVariantId; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getSellerSlug() { return sellerSlug; }
    public void setSellerSlug(String sellerSlug) { this.sellerSlug = sellerSlug; }
}
