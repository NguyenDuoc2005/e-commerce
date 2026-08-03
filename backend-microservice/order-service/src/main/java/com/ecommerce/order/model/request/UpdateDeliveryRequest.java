package com.ecommerce.order.model.request;

public class UpdateDeliveryRequest {
    private String maHoaDon;
    private String tenKhachHang;
    private String sdtKhachHang;
    private String email;
    private String diaChi;
    private Double tongTienSauGiam;
    private Double phiVanChuyen;

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
    public String getSdtKhachHang() { return sdtKhachHang; }
    public void setSdtKhachHang(String sdtKhachHang) { this.sdtKhachHang = sdtKhachHang; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public Double getTongTienSauGiam() { return tongTienSauGiam; }
    public void setTongTienSauGiam(Double tongTienSauGiam) { this.tongTienSauGiam = tongTienSauGiam; }
    public Double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(Double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }
}
