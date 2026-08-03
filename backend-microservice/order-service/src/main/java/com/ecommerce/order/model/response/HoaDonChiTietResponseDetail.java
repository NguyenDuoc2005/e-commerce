package com.ecommerce.order.model.response;

import java.time.LocalDateTime;

public interface HoaDonChiTietResponseDetail {
    String getMaHoaDon();
    String getTenHoaDon();
    String getMaHoaDonChiTiet();
    String getTenSanPham();
    String getAnhSanPham();
    String getThuongHieu();
    String getXuatSu();
    String getMauSac();
    String getSize();
    Integer getSoLuong();
    Double getGiaBan();
    Double getThanhTienSP();
    Double getThanhTien();
    String getTenKhachHang();
    String getSdtKH();
    String getEmail();
    String getDiaChi();
    String getTenKhachHang2();
    String getSdtKH2();
    String getEmail2();
    String getDiaChi2();
    String getLoaiHoaDon();
    String getTrangThaiHoaDon();
    LocalDateTime getThoiGian();
    Long getNgayTao();
    Double getPhiVanChuyen();
    String getMaVoucher();
    String getTenVoucher();
    Double getGiaTriVoucher();
    Double getTongTienSauGiam();
    Double getTongTien();
    String getPhuongThucThanhToan();
    Double getDuNo();
    Double getHoanPhi();
}
