package com.ecommerce.order.model.request;

import com.ecommerce.order.constant.EntityTrangThaiHoaDon;

public class ThanhToanRequest {
    private String hoaDonId;
    private Double soTienKhachDua;
    private Double soTienTraLai;
    private Double soTienGoc;
    private String ghiChu;
    private String loaiGiaoDich;
    private String nhanVienId;
    private EntityTrangThaiHoaDon trangThai;

    public String getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(String hoaDonId) { this.hoaDonId = hoaDonId; }
    public Double getSoTienKhachDua() { return soTienKhachDua; }
    public void setSoTienKhachDua(Double soTienKhachDua) { this.soTienKhachDua = soTienKhachDua; }
    public Double getSoTienTraLai() { return soTienTraLai; }
    public void setSoTienTraLai(Double soTienTraLai) { this.soTienTraLai = soTienTraLai; }
    public Double getSoTienGoc() { return soTienGoc; }
    public void setSoTienGoc(Double soTienGoc) { this.soTienGoc = soTienGoc; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getLoaiGiaoDich() { return loaiGiaoDich; }
    public void setLoaiGiaoDich(String loaiGiaoDich) { this.loaiGiaoDich = loaiGiaoDich; }
    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }
    public EntityTrangThaiHoaDon getTrangThai() { return trangThai; }
    public void setTrangThai(EntityTrangThaiHoaDon trangThai) { this.trangThai = trangThai; }
}
