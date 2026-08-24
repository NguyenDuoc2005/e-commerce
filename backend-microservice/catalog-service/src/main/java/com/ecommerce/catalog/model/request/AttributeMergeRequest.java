package com.ecommerce.catalog.model.request;

import jakarta.validation.constraints.NotBlank;

public class AttributeMergeRequest {
    @NotBlank
    private String targetId;
    private String reason;

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
