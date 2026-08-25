package com.ecommerce.seller.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "danh_gia", uniqueConstraints = @UniqueConstraint(
        columnNames = {"customer_id", "don_hang_seller_id", "product_detail_id"}))
public class Review {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(name = "product_id", length = 36)
    private String productId;

    @Column(name = "product_detail_id", nullable = false, length = 36)
    private String productDetailId;

    @Column(name = "don_hang_seller_id", nullable = false, length = 36)
    private String orderSellerId;

    @Column(name = "product_rating", nullable = false)
    private Integer productRating;

    @Column(name = "shop_rating", nullable = false)
    private Integer shopRating;

    @Lob
    @Column(name = "comment")
    private String comment;

    @Column(name = "image_urls", length = 4000)
    private String imageUrls;

    @Lob
    @Column(name = "seller_reply")
    private String sellerReply;

    @Column(name = "seller_replied_at")
    private Instant sellerRepliedAt;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        if (status == null || status.isBlank()) status = "VISIBLE";
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductDetailId() { return productDetailId; }
    public void setProductDetailId(String productDetailId) { this.productDetailId = productDetailId; }
    public String getOrderSellerId() { return orderSellerId; }
    public void setOrderSellerId(String orderSellerId) { this.orderSellerId = orderSellerId; }
    public Integer getProductRating() { return productRating; }
    public void setProductRating(Integer productRating) { this.productRating = productRating; }
    public Integer getShopRating() { return shopRating; }
    public void setShopRating(Integer shopRating) { this.shopRating = shopRating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public String getSellerReply() { return sellerReply; }
    public void setSellerReply(String sellerReply) { this.sellerReply = sellerReply; }
    public Instant getSellerRepliedAt() { return sellerRepliedAt; }
    public void setSellerRepliedAt(Instant sellerRepliedAt) { this.sellerRepliedAt = sellerRepliedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
