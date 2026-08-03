package com.ecommerce.order.model.response;

public class TopSanPhamBanChayResponse {
    private String id;
    private String maSanPham;
    private String tenSanPham;
    private String anhSanPham;
    private Long soLuongBan;
    private Double doanhThu;
    private String thuongHieu;
    private Double giaBan;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public String getAnhSanPham() { return anhSanPham; }
    public void setAnhSanPham(String anhSanPham) { this.anhSanPham = anhSanPham; }
    public Long getSoLuongBan() { return soLuongBan; }
    public void setSoLuongBan(Long soLuongBan) { this.soLuongBan = soLuongBan; }
    public Double getDoanhThu() { return doanhThu; }
    public void setDoanhThu(Double doanhThu) { this.doanhThu = doanhThu; }
    public String getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(String thuongHieu) { this.thuongHieu = thuongHieu; }
    public Double getGiaBan() { return giaBan; }
    public void setGiaBan(Double giaBan) { this.giaBan = giaBan; }
}
