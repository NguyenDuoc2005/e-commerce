package com.ecommerce.promotion.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FlashSaleRegistrationRequest {
    @NotBlank private String productVariantId;
    @NotNull @Positive private Double flashPrice;

    public String getProductVariantId() { return productVariantId; }
    public void setProductVariantId(String productVariantId) { this.productVariantId = productVariantId; }
    public Double getFlashPrice() { return flashPrice; }
    public void setFlashPrice(Double flashPrice) { this.flashPrice = flashPrice; }
}
