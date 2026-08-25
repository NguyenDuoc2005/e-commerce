package com.ecommerce.order.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class DisputeMessageRequest {
    @NotBlank private String message;
    private List<String> attachmentUrls;
    public String getMessage() { return message; } public void setMessage(String v) { message = v; }
    public List<String> getAttachmentUrls() { return attachmentUrls; } public void setAttachmentUrls(List<String> v) { attachmentUrls = v; }
}
