package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "san_pham")
public class SanPham extends PrimaryEntity {

    @Column(name = "ma_san_pham")
    private String ma;

    @Column(name = "ten_san_pham")
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
