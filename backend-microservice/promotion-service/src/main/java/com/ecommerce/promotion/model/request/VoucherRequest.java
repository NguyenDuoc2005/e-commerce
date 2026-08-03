package com.ecommerce.promotion.model.request;

import java.sql.Date;
import java.util.List;

public class VoucherRequest {
    private String id;
    private String ma;
    private String ten;
    private Double LoiPhanNay;
    private Integer soLuongPhieu;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private Double dieuKien;
    private Double giaGiam;
    private Boolean loaiGiam;
    private Boolean kieuGiam;
    private List<String> khachHangIds;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public Double getLoiPhanNay() { return LoiPhanNay; }
    public void setLoiPhanNay(Double loiPhanNay) { LoiPhanNay = loiPhanNay; }
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
    public List<String> getKhachHangIds() { return khachHangIds; }
    public void setKhachHangIds(List<String> khachHangIds) { this.khachHangIds = khachHangIds; }
}
