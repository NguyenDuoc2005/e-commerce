package com.ecommerce.order.model.request;

import com.ecommerce.order.constant.OrderStatusConstant;

public class ChangeStatusRequest {
    private String maOrder;
    private OrderStatusConstant status;
    private String note;

    public String getMaOrder() { return maOrder; }
    public void setMaOrder(String maOrder) { this.maOrder = maOrder; }
    public OrderStatusConstant getStatus() { return status; }
    public void setStatus(OrderStatusConstant status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
