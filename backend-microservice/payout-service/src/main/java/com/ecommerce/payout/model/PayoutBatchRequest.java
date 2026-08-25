package com.ecommerce.payout.model;

import java.util.List;

public class PayoutBatchRequest {
    private List<String> receivableIds;
    private String note;

    public List<String> getReceivableIds() { return receivableIds; }
    public void setReceivableIds(List<String> receivableIds) { this.receivableIds = receivableIds; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
