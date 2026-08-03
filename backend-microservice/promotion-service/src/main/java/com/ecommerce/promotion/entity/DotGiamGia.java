package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.constant.StatusPromotion;
import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "dot_giam_gia")
@DynamicUpdate
public class DotGiamGia extends PrimaryEntity {

    @Column(name = "ma_dot_giam_gia")
    private String ma;

    @Column(name = "ten_dot_giam_gia")
    private String ten;

    @Column(name = "phan_tram")
    private Double phanTramGiam;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ngay_bat_dau")
    private Long ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Long ngayKetThuc;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_dot")
    private StatusPromotion trangThai;

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public Double getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(Double phanTramGiam) { this.phanTramGiam = phanTramGiam; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Long getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Long ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public Long getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Long ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public StatusPromotion getTrangThai() { return trangThai; }
    public void setTrangThai(StatusPromotion trangThai) { this.trangThai = trangThai; }
}
