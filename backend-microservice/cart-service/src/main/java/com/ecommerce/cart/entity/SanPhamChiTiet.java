package com.ecommerce.cart.entity;

import com.ecommerce.cart.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "san_pham_chi_tiet")
public class SanPhamChiTiet extends PrimaryEntity {
    @Column(name = "ma_san_pham")
    private String ma;
    @Column(name = "gia_ban")
    private Double giaBan;
    @Column(name = "anh_san_pham")
    private String anh;
    @Column(name = "so_luong")
    private Integer soLuong;

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public Double getGiaBan() { return giaBan; }
    public void setGiaBan(Double giaBan) { this.giaBan = giaBan; }
    public String getAnh() { return anh; }
    public void setAnh(String anh) { this.anh = anh; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
}
