package com.ecommerce.order.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CheckoutRequest {
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private String ghiChu;
    private String email;
    private String maGiamGia;
    private String hinhThucThanhToan;
    private Double tongTien;
    private Double phiShip;
    private Double giamGia;
    private Double tongCong;
    private List<CheckoutProductItem> sanPham;

    @JsonProperty("KhachHang")
    private String khachHang;

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMaGiamGia() { return maGiamGia; }
    public void setMaGiamGia(String maGiamGia) { this.maGiamGia = maGiamGia; }
    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan) { this.hinhThucThanhToan = hinhThucThanhToan; }
    public Double getTongTien() { return tongTien; }
    public void setTongTien(Double tongTien) { this.tongTien = tongTien; }
    public Double getPhiShip() { return phiShip; }
    public void setPhiShip(Double phiShip) { this.phiShip = phiShip; }
    public Double getGiamGia() { return giamGia; }
    public void setGiamGia(Double giamGia) { this.giamGia = giamGia; }
    public Double getTongCong() { return tongCong; }
    public void setTongCong(Double tongCong) { this.tongCong = tongCong; }
    public List<CheckoutProductItem> getSanPham() { return sanPham; }
    public void setSanPham(List<CheckoutProductItem> sanPham) { this.sanPham = sanPham; }
    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }
}
