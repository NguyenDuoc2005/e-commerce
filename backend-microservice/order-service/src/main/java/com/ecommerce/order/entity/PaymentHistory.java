package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private Double soTien;

    @Column(name = "payment_time")
    private LocalDateTime thoiGian;

    @Column(name = "transaction_code")
    private String maGiaoDich;

    @Column(name = "transaction_type")
    private String loaiGiaoDich;

    @Column(name = "staff_id")
    private String nhanVienId;

    @Column(name = "order_id")
    private String hoaDonId;

    @Column(name = "note")
    private String ghiChu;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getSoTien() { return soTien; }
    public void setSoTien(Double soTien) { this.soTien = soTien; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
    public String getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(String maGiaoDich) { this.maGiaoDich = maGiaoDich; }
    public String getLoaiGiaoDich() { return loaiGiaoDich; }
    public void setLoaiGiaoDich(String loaiGiaoDich) { this.loaiGiaoDich = loaiGiaoDich; }
    public String getStaffId() { return nhanVienId; }
    public void setStaffId(String nhanVienId) { this.nhanVienId = nhanVienId; }
    public String getOrderId() { return hoaDonId; }
    public void setOrderId(String hoaDonId) { this.hoaDonId = hoaDonId; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
