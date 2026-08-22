package com.ecommerce.seller.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ReviewRequest {
    @NotBlank
    private String orderSellerId;
    @NotBlank
    private String productDetailId;
    @NotNull @Min(1) @Max(5)
    private Integer productRating;
    @NotNull @Min(1) @Max(5)
    private Integer shopRating;
    @Size(max = 2000)
    private String comment;
    @Size(max = 5)
    private List<String> imageUrls;

    public String getOrderSellerId() { return orderSellerId; }
    public void setOrderSellerId(String orderSellerId) { this.orderSellerId = orderSellerId; }
    public String getProductDetailId() { return productDetailId; }
    public void setProductDetailId(String productDetailId) { this.productDetailId = productDetailId; }
    public Integer getProductRating() { return productRating; }
    public void setProductRating(Integer productRating) { this.productRating = productRating; }
    public Integer getShopRating() { return shopRating; }
    public void setShopRating(Integer shopRating) { this.shopRating = shopRating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
