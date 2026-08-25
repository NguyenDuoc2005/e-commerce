package com.ecommerce.seller.model;

import jakarta.validation.constraints.NotBlank;

public class ResolveReportRequest {
    @NotBlank private String actionTaken;
    @NotBlank private String note;
    public String getActionTaken() { return actionTaken; } public void setActionTaken(String v) { actionTaken = v; }
    public String getNote() { return note; } public void setNote(String v) { note = v; }
}
