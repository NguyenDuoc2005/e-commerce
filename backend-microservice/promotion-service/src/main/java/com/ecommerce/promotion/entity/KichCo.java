package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "kich_co")
public class KichCo extends PrimaryEntity {
    @Column(name = "ma_kich_co")
    private String ma;
    @Column(name = "ten_kich_co")
    private String ten;

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
}
