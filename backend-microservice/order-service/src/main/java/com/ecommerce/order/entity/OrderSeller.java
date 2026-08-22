package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "order_seller")
public class OrderSeller {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_id", length = 36)
    private String orderId;

    @Column(name = "seller_id", length = 36)
    private String sellerId;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "seller_slug")
    private String sellerSlug;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "shipping_fee")
    private Double shippingFee;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "total_after_discount")
    private Double totalAfterDiscount;

    @Column(name = "order_status")
    private Integer orderStatus;

    @Column(name = "created_date")
    private Long createdDate;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (createdDate == null) {
            createdDate = System.currentTimeMillis();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getSellerSlug() { return sellerSlug; }
    public void setSellerSlug(String sellerSlug) { this.sellerSlug = sellerSlug; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getShippingFee() { return shippingFee; }
    public void setShippingFee(Double shippingFee) { this.shippingFee = shippingFee; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public Double getTotalAfterDiscount() { return totalAfterDiscount; }
    public void setTotalAfterDiscount(Double totalAfterDiscount) { this.totalAfterDiscount = totalAfterDiscount; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public Long getCreatedDate() { return createdDate; }
    public void setCreatedDate(Long createdDate) { this.createdDate = createdDate; }
}
