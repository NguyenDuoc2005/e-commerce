package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "ma_hoa_don")
    private String ma;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
}
