package com.ecommerce.promotion.model.request;

import jakarta.validation.constraints.NotBlank;

public class FlashSaleReviewRequest {
    @NotBlank private String decision;
    private String reason;

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
