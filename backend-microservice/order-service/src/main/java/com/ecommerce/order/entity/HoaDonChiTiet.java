package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "hoa_don_chi_tiet")
public class HoaDonChiTiet {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private Long createdDate;

    @Column(name = "ma_hoa_don_chi_tiet")
    private String ma;

    @Column(name = "ten_hoa_don_chi_tiet")
    private String ten;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_ban")
    private Double giaBan;

    @Column(name = "id_spct")
    private String sanPhamChiTietId;

    @Column(name = "id_hoa_don")
    private String hoaDonId;

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
    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public Double getGiaBan() { return giaBan; }
    public void setGiaBan(Double giaBan) { this.giaBan = giaBan; }
    public String getSanPhamChiTietId() { return sanPhamChiTietId; }
    public void setSanPhamChiTietId(String sanPhamChiTietId) { this.sanPhamChiTietId = sanPhamChiTietId; }
    public String getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(String hoaDonId) { this.hoaDonId = hoaDonId; }
}
