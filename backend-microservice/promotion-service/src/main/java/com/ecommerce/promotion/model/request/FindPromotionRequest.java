package com.ecommerce.promotion.model.request;

import com.ecommerce.common.base.PageableRequest;

public class FindPromotionRequest extends PageableRequest {
    private String id;
    private String ma;
    private String ten;
    private Double phanTramGiam;
    private Long ngayBatDau;
    private Long ngayKetThuc;
    private String trangThai;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public Double getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(Double phanTramGiam) { this.phanTramGiam = phanTramGiam; }
    public Long getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Long ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public Long getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Long ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
