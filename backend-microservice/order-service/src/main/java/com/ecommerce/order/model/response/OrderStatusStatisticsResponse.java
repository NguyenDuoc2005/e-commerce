package com.ecommerce.order.model.response;

public class OrderStatusStatisticsResponse {
    private String trangThai;
    private long quantity;
    private double tiLePhanTram;

    public OrderStatusStatisticsResponse(String trangThai, long quantity, double tiLePhanTram) {
        this.trangThai = trangThai;
        this.quantity = quantity;
        this.tiLePhanTram = tiLePhanTram;
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public double getTiLePhanTram() { return tiLePhanTram; }
    public void setTiLePhanTram(double tiLePhanTram) { this.tiLePhanTram = tiLePhanTram; }
}
