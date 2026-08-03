package com.ecommerce.order.model.response;

public class ThongKeDonHangResponse {
    private String date;
    private Long soLuongDonHang;

    public ThongKeDonHangResponse() {
    }

    public ThongKeDonHangResponse(String date, Long soLuongDonHang) {
        this.date = date;
        this.soLuongDonHang = soLuongDonHang;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Long getSoLuongDonHang() { return soLuongDonHang; }
    public void setSoLuongDonHang(Long soLuongDonHang) { this.soLuongDonHang = soLuongDonHang; }
}
