package com.ecommerce.order.entity;

import com.ecommerce.order.constant.OrderTypeConstant;
import com.ecommerce.order.constant.EntityPhuongThucThanhToan;
import com.ecommerce.order.constant.OrderStatusConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private Long createdDate;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "customer_phone")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "customer_name")
    private String tenKH;

    @Column(name = "shipping_fee")
    private Double phiVanCdistrict;

    @Column(name = "shipping_address")
    private String address;

    @Column(name = "total_after_discount")
    private Double tongTienSauGiam;

    @Column(name = "total_amount")
    private Double tongTien;

    @Column(name = "discount_amount")
    private Double giamGia;

    @Column(name = "debt_amount")
    private Double duNo;

    @Column(name = "refund_amount")
    private Double hoanPhi;

    @Column(name = "note")
    private String ghiChu;

    @Column(name = "payment_method")
    private EntityPhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "order_type")
    private OrderTypeConstant loaiOrder;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "voucher_id")
    private String voucherId;

    @Column(name = "staff_id")
    private String nhanVienId;

    @Column(name = "order_status")
    private OrderStatusConstant trangThaiOrder;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedDate() { return createdDate; }
    public void setCreatedDate(Long createdDate) { this.createdDate = createdDate; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }
    public Double getPhiVanCdistrict() { return phiVanCdistrict; }
    public void setPhiVanCdistrict(Double phiVanCdistrict) { this.phiVanCdistrict = phiVanCdistrict; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getTongTienSauGiam() { return tongTienSauGiam; }
    public void setTongTienSauGiam(Double tongTienSauGiam) { this.tongTienSauGiam = tongTienSauGiam; }
    public Double getTongTien() { return tongTien; }
    public void setTongTien(Double tongTien) { this.tongTien = tongTien; }
    public Double getGiamGia() { return giamGia; }
    public void setGiamGia(Double giamGia) { this.giamGia = giamGia; }
    public Double getDuNo() { return duNo; }
    public void setDuNo(Double duNo) { this.duNo = duNo; }
    public Double getHoanPhi() { return hoanPhi; }
    public void setHoanPhi(Double hoanPhi) { this.hoanPhi = hoanPhi; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public EntityPhuongThucThanhToan getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(EntityPhuongThucThanhToan phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public OrderTypeConstant getLoaiOrder() { return loaiOrder; }
    public void setLoaiOrder(OrderTypeConstant loaiOrder) { this.loaiOrder = loaiOrder; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public String getStaffId() { return nhanVienId; }
    public void setStaffId(String nhanVienId) { this.nhanVienId = nhanVienId; }
    public OrderStatusConstant getTrangThaiOrder() { return trangThaiOrder; }
    public void setTrangThaiOrder(OrderStatusConstant trangThaiOrder) { this.trangThaiOrder = trangThaiOrder; }
}
