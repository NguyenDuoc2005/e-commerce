package com.ecommerce.cart.entity;

import com.ecommerce.cart.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "gio_hang")
public class Cart extends PrimaryEntity {
    @Column(name = "id_khach_hang")
    private String khachHangId;

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }
}
