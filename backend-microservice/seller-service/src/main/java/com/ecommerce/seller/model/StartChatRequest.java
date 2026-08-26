package com.ecommerce.seller.model;

import jakarta.validation.constraints.NotBlank;

public class StartChatRequest {
    @NotBlank
    private String sellerId;
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
}
