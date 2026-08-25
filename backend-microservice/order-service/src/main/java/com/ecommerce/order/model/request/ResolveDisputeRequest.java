package com.ecommerce.order.model.request;

import jakarta.validation.constraints.NotBlank;

public class ResolveDisputeRequest {
    @NotBlank private String decision;
    private Double resolvedAmount;
    @NotBlank private String note;
    public String getDecision() { return decision; } public void setDecision(String v) { decision = v; }
    public Double getResolvedAmount() { return resolvedAmount; } public void setResolvedAmount(Double v) { resolvedAmount = v; }
    public String getNote() { return note; } public void setNote(String v) { note = v; }
}
