package com.ecommerce.order.entity;

import com.ecommerce.order.constant.OrderStatusConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private String hoaDonId;

    @Column(name = "status")
    private OrderStatusConstant trangThai;

    @Column(name = "payment_time")
    private LocalDateTime thoiGian;

    @Column(name = "note")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderId() { return hoaDonId; }
    public void setOrderId(String hoaDonId) { this.hoaDonId = hoaDonId; }
    public OrderStatusConstant getTrangThai() { return trangThai; }
    public void setTrangThai(OrderStatusConstant trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
