package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "phieu_giam_gia_chi_tiet_khach_hang")
@DynamicUpdate
public class PhieuGiamGiaChiTiet extends PrimaryEntity implements Serializable {

    @Column(name = "ma_phieu_giam_gia_chi_tiet")
    private String ma;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", referencedColumnName = "id")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_phieu_giam_gia", referencedColumnName = "id")
    private PhieuGiamGia phieuGiamGia;

    @PrePersist
    void generateCode() {
        if (ma == null || ma.isBlank()) {
            ma = "PGGCT-" + UUID.randomUUID();
        }
    }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public PhieuGiamGia getPhieuGiamGia() { return phieuGiamGia; }
    public void setPhieuGiamGia(PhieuGiamGia phieuGiamGia) { this.phieuGiamGia = phieuGiamGia; }
}
