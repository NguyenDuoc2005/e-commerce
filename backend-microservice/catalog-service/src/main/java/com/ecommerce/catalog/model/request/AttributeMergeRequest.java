package com.ecommerce.catalog.model.request;

public class AttributeMergeRequest {
    private String targetAttributeId;
    private String reason;

    public String getTargetAttributeId() { return targetAttributeId; }
    public void setTargetAttributeId(String targetAttributeId) { this.targetAttributeId = targetAttributeId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
