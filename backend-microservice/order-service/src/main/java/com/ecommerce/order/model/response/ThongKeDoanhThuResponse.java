package com.ecommerce.order.model.response;

public class ThongKeDoanhThuResponse {
    private Double doanhSoThangNay;
    private Integer soOrderThangNay;
    private Double doanhSoHomNay;
    private Integer soOrderHomNay;
    private Integer hangBanDuocThangNay;

    public ThongKeDoanhThuResponse() {
    }

    public ThongKeDoanhThuResponse(Double doanhSoThangNay, Integer soOrderThangNay, Double doanhSoHomNay, Integer soOrderHomNay, Integer hangBanDuocThangNay) {
        this.doanhSoThangNay = doanhSoThangNay;
        this.soOrderThangNay = soOrderThangNay;
        this.doanhSoHomNay = doanhSoHomNay;
        this.soOrderHomNay = soOrderHomNay;
        this.hangBanDuocThangNay = hangBanDuocThangNay;
    }

    public Double getDoanhSoThangNay() { return doanhSoThangNay; }
    public void setDoanhSoThangNay(Double doanhSoThangNay) { this.doanhSoThangNay = doanhSoThangNay; }
    public Integer getSoOrderThangNay() { return soOrderThangNay; }
    public void setSoOrderThangNay(Integer soOrderThangNay) { this.soOrderThangNay = soOrderThangNay; }
    public Double getDoanhSoHomNay() { return doanhSoHomNay; }
    public void setDoanhSoHomNay(Double doanhSoHomNay) { this.doanhSoHomNay = doanhSoHomNay; }
    public Integer getSoOrderHomNay() { return soOrderHomNay; }
    public void setSoOrderHomNay(Integer soOrderHomNay) { this.soOrderHomNay = soOrderHomNay; }
    public Integer getHangBanDuocThangNay() { return hangBanDuocThangNay; }
    public void setHangBanDuocThangNay(Integer hangBanDuocThangNay) { this.hangBanDuocThangNay = hangBanDuocThangNay; }
}
