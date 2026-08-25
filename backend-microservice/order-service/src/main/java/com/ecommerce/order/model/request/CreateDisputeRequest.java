package com.ecommerce.order.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateDisputeRequest {
    @NotBlank private String orderSellerId;
    @NotBlank private String disputeType;
    @NotBlank private String reason;
    private String description;
    private List<String> evidenceUrls;
    private Double requestedAmount;
    public String getOrderSellerId() { return orderSellerId; } public void setOrderSellerId(String v) { orderSellerId = v; }
    public String getDisputeType() { return disputeType; } public void setDisputeType(String v) { disputeType = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public List<String> getEvidenceUrls() { return evidenceUrls; } public void setEvidenceUrls(List<String> v) { evidenceUrls = v; }
    public Double getRequestedAmount() { return requestedAmount; } public void setRequestedAmount(Double v) { requestedAmount = v; }
}
