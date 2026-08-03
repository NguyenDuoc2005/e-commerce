package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.CatalogAttribute;
import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Table(name = "xuat_su")
@DynamicUpdate
public class XuatSu extends PrimaryEntity implements CatalogAttribute, Serializable {

    @Column(name = "ma_xuat_su")
    private String ma;

    @Column(name = "ten_xuat_su")
    private String ten;

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
}
