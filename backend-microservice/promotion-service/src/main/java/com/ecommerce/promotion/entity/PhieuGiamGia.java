package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

@Entity
@Table(name = "phieu_giam_gia")
@DynamicUpdate
public class PhieuGiamGia extends PrimaryEntity implements Serializable {

    @Column(name = "ma_phieu_giam_gia")
    private String ma;

    @Column(name = "ten_phieu_giam_gia")
    private String ten;

    @Column(name = "phan_tram")
    private Double phanTramGiam;

    @Transient
    private Double giaTriGiamThucTe;

    @Column(name = "so_luong_phieu")
    private Integer soLuongPhieu;

    @Column(name = "ngay_bat_dau")
    private Date ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Date ngayKetThuc;

    @Column(name = "dieu_kien")
    private Double dieuKien;

    @Column(name = "gia_giam_toi_da")
    private Double giaGiam;

    @Column(name = "loai_giam")
    private Boolean loaiGiam;

    @Column(name = "kieu_giam")
    private Boolean kieuGiam;

    @PrePersist
    void generateCode() {
        if (ma == null || ma.isBlank()) {
            ma = String.format("PGG%04d", new Random().nextInt(10000));
        }
    }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public Double getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(Double phanTramGiam) { this.phanTramGiam = phanTramGiam; }
    public Double getGiaTriGiamThucTe() { return giaTriGiamThucTe; }
    public void setGiaTriGiamThucTe(Double giaTriGiamThucTe) { this.giaTriGiamThucTe = giaTriGiamThucTe; }
    public Integer getSoLuongPhieu() { return soLuongPhieu; }
    public void setSoLuongPhieu(Integer soLuongPhieu) { this.soLuongPhieu = soLuongPhieu; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public Double getDieuKien() { return dieuKien; }
    public void setDieuKien(Double dieuKien) { this.dieuKien = dieuKien; }
    public Double getGiaGiam() { return giaGiam; }
    public void setGiaGiam(Double giaGiam) { this.giaGiam = giaGiam; }
    public Boolean getLoaiGiam() { return loaiGiam; }
    public void setLoaiGiam(Boolean loaiGiam) { this.loaiGiam = loaiGiam; }
    public Boolean getKieuGiam() { return kieuGiam; }
    public void setKieuGiam(Boolean kieuGiam) { this.kieuGiam = kieuGiam; }
}
