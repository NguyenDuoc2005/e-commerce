package com.ecommerce.order.model.response;

public class ThongKeTrangThaiHoaDonResponse {
    private String trangThai;
    private long soLuong;
    private double tiLePhanTram;

    public ThongKeTrangThaiHoaDonResponse(String trangThai, long soLuong, double tiLePhanTram) {
        this.trangThai = trangThai;
        this.soLuong = soLuong;
        this.tiLePhanTram = tiLePhanTram;
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public long getSoLuong() { return soLuong; }
    public void setSoLuong(long soLuong) { this.soLuong = soLuong; }
    public double getTiLePhanTram() { return tiLePhanTram; }
    public void setTiLePhanTram(double tiLePhanTram) { this.tiLePhanTram = tiLePhanTram; }
}
