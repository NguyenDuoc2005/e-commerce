package com.ecommerce.order.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.model.request.BanHangRequest;

import java.util.List;
import java.util.Map;

public interface BanHangService {
    Map<String, Object> availableVouchers(String idHD, String idKH, Double tongTien);
    List<Map<String, Object>> getHoaDon();
    ResponseObject<?> createHoaDon(BanHangRequest request);
    ResponseObject<?> huy(BanHangRequest request);
    ResponseObject<?> themSanPham(BanHangRequest request);
    List<Map<String, Object>> getListGioHang(String id);
    void xoaSanPham(BanHangRequest request);
    ResponseObject<?> themSoLuong(BanHangRequest request);
    void xoaSoLuong(BanHangRequest request);
    ResponseObject<?> listKhachHang(BanHangRequest request);
    void themKhachHang(BanHangRequest request);
    ResponseObject<?> themMoiKhachHang(BanHangRequest request);
    Map<String, Object> getKhachHang(String id);
    Map<String, Object> getThanhToan(String id);
    List<Map<String, Object>> getPhuongThucThanhToan(String id);
    void capNhatPhuongThucThanhToan(BanHangRequest request);
    ResponseObject<?> getAllSanPham(BanHangRequest request);
    ResponseObject<?> thanhToanThanhCong(BanHangRequest request);
    ResponseObject<?> danhSachPhieuGiamGia(BanHangRequest request);
    ResponseObject<?> giaoHang(String id);
}
