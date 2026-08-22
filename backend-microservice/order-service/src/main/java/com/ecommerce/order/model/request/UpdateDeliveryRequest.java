package com.ecommerce.order.model.request;

public class UpdateDeliveryRequest {
    private String maOrder;
    private String tenCustomer;
    private String sdtCustomer;
    private String email;
    private String address;
    private Double tongTienSauGiam;
    private Double phiVanCdistrict;

    public String getMaOrder() { return maOrder; }
    public void setMaOrder(String maOrder) { this.maOrder = maOrder; }
    public String getTenCustomer() { return tenCustomer; }
    public void setTenCustomer(String tenCustomer) { this.tenCustomer = tenCustomer; }
    public String getSdtCustomer() { return sdtCustomer; }
    public void setSdtCustomer(String sdtCustomer) { this.sdtCustomer = sdtCustomer; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getTongTienSauGiam() { return tongTienSauGiam; }
    public void setTongTienSauGiam(Double tongTienSauGiam) { this.tongTienSauGiam = tongTienSauGiam; }
    public Double getPhiVanCdistrict() { return phiVanCdistrict; }
    public void setPhiVanCdistrict(Double phiVanCdistrict) { this.phiVanCdistrict = phiVanCdistrict; }
}
