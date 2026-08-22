package com.ecommerce.order.model.response;

public class TopSellingProductResponse {
    private String id;
    private String maProduct;
    private String tenProduct;
    private String anhProduct;
    private Long soLuongBan;
    private Double doanhThu;
    private String brand;
    private Double salePrice;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaProduct() { return maProduct; }
    public void setMaProduct(String maProduct) { this.maProduct = maProduct; }
    public String getTenProduct() { return tenProduct; }
    public void setTenProduct(String tenProduct) { this.tenProduct = tenProduct; }
    public String getAnhProduct() { return anhProduct; }
    public void setAnhProduct(String anhProduct) { this.anhProduct = anhProduct; }
    public Long getSoLuongBan() { return soLuongBan; }
    public void setSoLuongBan(Long soLuongBan) { this.soLuongBan = soLuongBan; }
    public Double getDoanhThu() { return doanhThu; }
    public void setDoanhThu(Double doanhThu) { this.doanhThu = doanhThu; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }
}
