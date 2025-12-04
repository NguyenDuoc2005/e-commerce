package com.be.server.core.admin.banhang.service;

import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.banhang.model.request.ADCapNhatPhuongTHucThanhToanRequest;
import com.be.server.core.admin.banhang.model.request.ADHuyRequest;
import com.be.server.core.admin.banhang.model.request.ADNhanVienRequest;
import com.be.server.core.admin.banhang.model.request.ADThanhToanRequest;
import com.be.server.core.admin.banhang.model.request.ADThemGioHangRequest;
import com.be.server.core.admin.banhang.model.request.ADThemKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.ADThemSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ADXoaSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ChonPhieuGiamGiaRequest;
import com.be.server.core.admin.banhang.model.request.ListKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.ListSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ThemMoiKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.giaoHangRequest;
import com.be.server.core.admin.banhang.model.response.ADChonKhachHangRespones;
import com.be.server.core.admin.banhang.model.response.ADGioHangRespones;
import com.be.server.core.admin.banhang.model.response.ADPhuongThucThanhToanRespones;
import com.be.server.core.admin.banhang.model.response.ADThanhToanRespones;
import com.be.server.core.admin.banhang.model.response.ListHoaDon;
import com.be.server.core.admin.loaide.model.request.ADLoaiDeRequest;
import com.be.server.core.admin.nhanvien.repository.ADNhanVienRepository;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.HoaDon;
import com.be.server.entity.KhachHang;
import com.be.server.entity.PhieuGiamGia;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ADBanHangService {

    List<ListHoaDon> getHoaDon();

    ResponseObject<?> createHoaDon(ADNhanVienRequest adNhanVienRequest);

    ResponseObject<?> createThemSanPham(ADThemSanPhamRequest adThemSanPhamRequest);

    List<ADGioHangRespones> getListGioHang(String id);

    void xoaHoaDonChiTiet(ADXoaSanPhamRequest adXoaSanPhamRequest);

    ResponseObject<?> ThemSoLuong(ADXoaSanPhamRequest adXoaSanPhamRequest);

    void xoaSoLuong(ADXoaSanPhamRequest adXoaSanPhamRequest);

    ResponseObject<?> listKhachHang(ListKhachHangRequest request );

    void themKhachHang(ADThemKhachHangRequest id);

    ResponseObject<?> themMoiKhachHang(ThemMoiKhachHangRequest adXoaSanPhamRequest);

    ADChonKhachHangRespones getKhachHang(String id);

    ADThanhToanRespones getThanhToan(String id);

    List<ADPhuongThucThanhToanRespones> getPhuongThucThanhToan(String id);

    void capNhatPhuongThucThanhToan(ADCapNhatPhuongTHucThanhToanRequest id);

    ResponseObject<?> getAll(ListSanPhamRequest id);

    ResponseObject<?> thanhToanThanhCong(ADThanhToanRequest id);

    ResponseObject<?> danhSachPhieuGiamGia(ChonPhieuGiamGiaRequest id);

    List<PhieuGiamGia> danhSachPhieuGiamGia1(ChonPhieuGiamGiaRequest id);

    ResponseObject<?> danhSachPhieuGiamGiaKoDuDieuKien(ChonPhieuGiamGiaRequest id);

    ResponseObject<?> giaoHang(String request);

    ResponseObject<?> huy(ADHuyRequest request);
}