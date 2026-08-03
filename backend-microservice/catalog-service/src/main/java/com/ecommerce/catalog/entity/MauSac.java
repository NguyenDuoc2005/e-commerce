package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.CatalogAttribute;
import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Table(name = "mau_sac")
@DynamicUpdate
public class MauSac extends PrimaryEntity implements CatalogAttribute, Serializable {

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
