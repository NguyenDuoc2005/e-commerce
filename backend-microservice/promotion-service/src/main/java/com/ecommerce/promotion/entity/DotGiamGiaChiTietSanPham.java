package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.constant.Status;
import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "dot_giam_gia_chi_tiet_san_pham")
@DynamicUpdate
public class DotGiamGiaChiTietSanPham extends PrimaryEntity {

    @Column(name = "ma_dot_giam_gia_chi_tiet_san_pham")
    private String ma;

    @Column(name = "gia_truoc_khi_giam")
    private Double giaTruoc;

    @Column(name = "gia_sau_khi_giam")
    private Double giaSau;

    @Enumerated(EnumType.STRING)
    private Status trangThai;

    @ManyToOne
    @JoinColumn(name = "id_chi_tiet_san_pham", referencedColumnName = "id")
    private SanPhamChiTiet sanPhamChiTiet;

    @ManyToOne
    @JoinColumn(name = "id_dot_giam_gia", referencedColumnName = "id")
    private DotGiamGia dotGiamGia;

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public Double getGiaTruoc() { return giaTruoc; }
    public void setGiaTruoc(Double giaTruoc) { this.giaTruoc = giaTruoc; }
    public Double getGiaSau() { return giaSau; }
    public void setGiaSau(Double giaSau) { this.giaSau = giaSau; }
    public Status getTrangThai() { return trangThai; }
    public void setTrangThai(Status trangThai) { this.trangThai = trangThai; }
    public SanPhamChiTiet getSanPhamChiTiet() { return sanPhamChiTiet; }
    public void setSanPhamChiTiet(SanPhamChiTiet sanPhamChiTiet) { this.sanPhamChiTiet = sanPhamChiTiet; }
    public DotGiamGia getDotGiamGia() { return dotGiamGia; }
    public void setDotGiamGia(DotGiamGia dotGiamGia) { this.dotGiamGia = dotGiamGia; }
}
