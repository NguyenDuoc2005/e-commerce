package com.ecommerce.cart.entity;

import com.ecommerce.cart.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "khach_hang")
public class KhachHang extends PrimaryEntity {
    @Column(name = "ten_khach_hang")
    private String ten;
    @Column(name = "email")
    private String email;

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
