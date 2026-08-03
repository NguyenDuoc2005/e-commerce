package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "mau_sac")
public class MauSac extends PrimaryEntity {
    @Column(name = "ma_mau_sac")
    private String ma;
    @Column(name = "ten_mau_sac")
    private String ten;
    @Column(name = "mau_sac")
    private String mau;

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getMau() { return mau; }
    public void setMau(String mau) { this.mau = mau; }
}
