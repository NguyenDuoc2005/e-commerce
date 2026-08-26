package com.ecommerce.seller.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_conversation",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_customer_seller", columnNames = {"customer_id", "seller_id"}),
        indexes = {
                @Index(name = "idx_chat_customer_last", columnList = "customer_id,last_message_at"),
                @Index(name = "idx_chat_seller_last", columnList = "seller_id,last_message_at")
        })
public class ChatConversation {
    @Id @Column(length = 36)
    private String id;
    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;
    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;
    @Column(name = "buyer_name", nullable = false)
    private String buyerName;
    @Column(name = "shop_name", nullable = false)
    private String shopName;
    @Column(name = "shop_logo_url", length = 1000)
    private String shopLogoUrl;
    @Column(name = "last_message", length = 500)
    private String lastMessage;
    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;
    @Column(name = "buyer_unread_count", nullable = false)
    private Integer buyerUnreadCount = 0;
    @Column(name = "seller_unread_count", nullable = false)
    private Integer sellerUnreadCount = 0;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void onCreate() {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (lastMessageAt == null) lastMessageAt = now;
        updatedAt = now;
    }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getShopLogoUrl() { return shopLogoUrl; }
    public void setShopLogoUrl(String shopLogoUrl) { this.shopLogoUrl = shopLogoUrl; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public Integer getBuyerUnreadCount() { return buyerUnreadCount; }
    public void setBuyerUnreadCount(Integer buyerUnreadCount) { this.buyerUnreadCount = buyerUnreadCount; }
    public Integer getSellerUnreadCount() { return sellerUnreadCount; }
    public void setSellerUnreadCount(Integer sellerUnreadCount) { this.sellerUnreadCount = sellerUnreadCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
