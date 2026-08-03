package com.ecommerce.order.model.request;

import com.ecommerce.order.constant.EntityTrangThaiHoaDon;

public class ChangeStatusRequest {
    private String maHoaDon;
    private EntityTrangThaiHoaDon status;
    private String note;

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public EntityTrangThaiHoaDon getStatus() { return status; }
    public void setStatus(EntityTrangThaiHoaDon status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
