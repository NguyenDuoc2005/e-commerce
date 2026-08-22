package com.ecommerce.cart.model.request;

public class CartDetailRequest {
    private String idCustomer;
    private String idSPCT;
    private String price;
    private String quantity;

    public String getIdCustomer() { return idCustomer; }
    public void setIdCustomer(String idCustomer) { this.idCustomer = idCustomer; }
    public String getIdSPCT() { return idSPCT; }
    public void setIdSPCT(String idSPCT) { this.idSPCT = idSPCT; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}
