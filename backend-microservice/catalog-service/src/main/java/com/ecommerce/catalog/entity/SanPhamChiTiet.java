package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Random;

@Entity
@Table(name = "san_pham_chi_tiet")
@DynamicUpdate
public class SanPhamChiTiet extends PrimaryEntity implements Serializable {

    @Column(name = "ma_san_pham")
    private String ma;

    @Column(name = "gia_ban")
    private Double giaBan;

    @Column(name = "anh_san_pham")
    private String anh;

    @Column(name = "so_luong")
    private Integer soLuong;

    @ManyToOne
    @JoinColumn(name = "id_san_pham", referencedColumnName = "id")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_kich_co", referencedColumnName = "id")
    private KichCo kichCo;

    @ManyToOne
    @JoinColumn(name = "id_mau_sac", referencedColumnName = "id")
    private MauSac mauSac;

    @PrePersist
    void generateCode() {
        if (ma == null || ma.isBlank()) {
            ma = String.format("SPCT%04d", new Random().nextInt(10000));
        }
    }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public Double getGiaBan() { return giaBan; }
    public void setGiaBan(Double giaBan) { this.giaBan = giaBan; }
    public String getAnh() { return anh; }
    public void setAnh(String anh) { this.anh = anh; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }
    public KichCo getKichCo() { return kichCo; }
    public void setKichCo(KichCo kichCo) { this.kichCo = kichCo; }
    public MauSac getMauSac() { return mauSac; }
    public void setMauSac(MauSac mauSac) { this.mauSac = mauSac; }
}
