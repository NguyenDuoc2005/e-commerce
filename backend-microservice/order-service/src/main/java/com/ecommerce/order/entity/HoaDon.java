package com.ecommerce.order.entity;

import com.ecommerce.order.constant.EntityLoaiHoaDon;
import com.ecommerce.order.constant.EntityPhuongThucThanhToan;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private Long createdDate;

    @Column(name = "ma_hoa_don")
    private String ma;

    @Column(name = "ten_hoa_don")
    private String ten;

    @Column(name = "so_dien_thoai_khach_hang")
    private String sdt;

    @Column(name = "email")
    private String email;

    @Column(name = "ten_khach_hang")
    private String tenKH;

    @Column(name = "phi_van_chuyen")
    private Double phiVanChuyen;

    @Column(name = "dia_chi_giao_hang")
    private String diaChi;

    @Column(name = "tong_tien_sau_giam")
    private Double tongTienSauGiam;

    @Column(name = "tong_tien")
    private Double tongTien;

    @Column(name = "giam_gia")
    private Double giamGia;

    @Column(name = "du_no")
    private Double duNo;

    @Column(name = "hoan_phi")
    private Double hoanPhi;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "phuong_thuc_thanh_toan")
    private EntityPhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "loai_hoa_don")
    private EntityLoaiHoaDon loaiHoaDon;

    @Column(name = "id_khach_hang")
    private String khachHangId;

    @Column(name = "id_voucher")
    private String voucherId;

    @Column(name = "id_nhan_vien")
    private String nhanVienId;

    @Column(name = "trang_thai_hoa_don")
    private EntityTrangThaiHoaDon trangThaiHoaDon;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedDate() { return createdDate; }
    public void setCreatedDate(Long createdDate) { this.createdDate = createdDate; }
    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }
    public Double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(Double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public Double getTongTienSauGiam() { return tongTienSauGiam; }
    public void setTongTienSauGiam(Double tongTienSauGiam) { this.tongTienSauGiam = tongTienSauGiam; }
    public Double getTongTien() { return tongTien; }
    public void setTongTien(Double tongTien) { this.tongTien = tongTien; }
    public Double getGiamGia() { return giamGia; }
    public void setGiamGia(Double giamGia) { this.giamGia = giamGia; }
    public Double getDuNo() { return duNo; }
    public void setDuNo(Double duNo) { this.duNo = duNo; }
    public Double getHoanPhi() { return hoanPhi; }
    public void setHoanPhi(Double hoanPhi) { this.hoanPhi = hoanPhi; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public EntityPhuongThucThanhToan getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(EntityPhuongThucThanhToan phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public EntityLoaiHoaDon getLoaiHoaDon() { return loaiHoaDon; }
    public void setLoaiHoaDon(EntityLoaiHoaDon loaiHoaDon) { this.loaiHoaDon = loaiHoaDon; }
    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }
    public EntityTrangThaiHoaDon getTrangThaiHoaDon() { return trangThaiHoaDon; }
    public void setTrangThaiHoaDon(EntityTrangThaiHoaDon trangThaiHoaDon) { this.trangThaiHoaDon = trangThaiHoaDon; }
}
