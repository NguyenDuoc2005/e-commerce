package com.ecommerce.seller.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateReportRequest {
    @NotBlank private String targetType;
    @NotBlank private String targetId;
    @NotBlank private String reasonCode;
    private String description;
    private List<String> evidenceUrls;
    public String getTargetType() { return targetType; } public void setTargetType(String v) { targetType = v; }
    public String getTargetId() { return targetId; } public void setTargetId(String v) { targetId = v; }
    public String getReasonCode() { return reasonCode; } public void setReasonCode(String v) { reasonCode = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public List<String> getEvidenceUrls() { return evidenceUrls; } public void setEvidenceUrls(List<String> v) { evidenceUrls = v; }
}
