package com.ecommerce.cart.model.request;

public class CartDetailRequest {
    private String idKhachHang;
    private String idSPCT;
    private String price;
    private String quantity;

    public String getIdKhachHang() { return idKhachHang; }
    public void setIdKhachHang(String idKhachHang) { this.idKhachHang = idKhachHang; }
    public String getIdSPCT() { return idSPCT; }
    public void setIdSPCT(String idSPCT) { this.idSPCT = idSPCT; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}
