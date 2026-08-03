package com.ecommerce.order.model.request;

import com.ecommerce.common.base.PageableRequest;

public class HoaDonDetailRequest extends PageableRequest {
    private String maHoaDon;

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
}
