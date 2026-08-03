package com.ecommerce.order.model.response;

public class ThongKeDoanhThuResponse {
    private Double doanhSoThangNay;
    private Integer soHoaDonThangNay;
    private Double doanhSoHomNay;
    private Integer soHoaDonHomNay;
    private Integer hangBanDuocThangNay;

    public ThongKeDoanhThuResponse() {
    }

    public ThongKeDoanhThuResponse(Double doanhSoThangNay, Integer soHoaDonThangNay, Double doanhSoHomNay, Integer soHoaDonHomNay, Integer hangBanDuocThangNay) {
        this.doanhSoThangNay = doanhSoThangNay;
        this.soHoaDonThangNay = soHoaDonThangNay;
        this.doanhSoHomNay = doanhSoHomNay;
        this.soHoaDonHomNay = soHoaDonHomNay;
        this.hangBanDuocThangNay = hangBanDuocThangNay;
    }

    public Double getDoanhSoThangNay() { return doanhSoThangNay; }
    public void setDoanhSoThangNay(Double doanhSoThangNay) { this.doanhSoThangNay = doanhSoThangNay; }
    public Integer getSoHoaDonThangNay() { return soHoaDonThangNay; }
    public void setSoHoaDonThangNay(Integer soHoaDonThangNay) { this.soHoaDonThangNay = soHoaDonThangNay; }
    public Double getDoanhSoHomNay() { return doanhSoHomNay; }
    public void setDoanhSoHomNay(Double doanhSoHomNay) { this.doanhSoHomNay = doanhSoHomNay; }
    public Integer getSoHoaDonHomNay() { return soHoaDonHomNay; }
    public void setSoHoaDonHomNay(Integer soHoaDonHomNay) { this.soHoaDonHomNay = soHoaDonHomNay; }
    public Integer getHangBanDuocThangNay() { return hangBanDuocThangNay; }
    public void setHangBanDuocThangNay(Integer hangBanDuocThangNay) { this.hangBanDuocThangNay = hangBanDuocThangNay; }
}
