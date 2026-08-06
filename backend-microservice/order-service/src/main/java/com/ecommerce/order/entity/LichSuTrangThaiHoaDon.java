package com.ecommerce.order.entity;

import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_trang_thai_hoa_don")
public class LichSuTrangThaiHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hoa_don_id")
    private String hoaDonId;

    @Column(name = "trang_thai")
    private EntityTrangThaiHoaDon trangThai;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "note")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(String hoaDonId) { this.hoaDonId = hoaDonId; }
    public EntityTrangThaiHoaDon getTrangThai() { return trangThai; }
    public void setTrangThai(EntityTrangThaiHoaDon trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
