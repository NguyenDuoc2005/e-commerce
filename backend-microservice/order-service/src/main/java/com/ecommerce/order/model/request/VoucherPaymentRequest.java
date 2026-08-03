package com.ecommerce.order.model.request;

public class VoucherPaymentRequest {
    private String idKH;
    private String maPGG;
    private Double tongTien;

    public String getIdKH() { return idKH; }
    public void setIdKH(String idKH) { this.idKH = idKH; }
    public String getMaPGG() { return maPGG; }
    public void setMaPGG(String maPGG) { this.maPGG = maPGG; }
    public Double getTongTien() { return tongTien; }
    public void setTongTien(Double tongTien) { this.tongTien = tongTien; }
}
