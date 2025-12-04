package com.be.server.core.admin.banhang.controller;

import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.banhang.model.request.ADCapNhatPhuongTHucThanhToanRequest;
import com.be.server.core.admin.banhang.model.request.ADHuyRequest;
import com.be.server.core.admin.banhang.model.request.ADNhanVienRequest;
import com.be.server.core.admin.banhang.model.request.ADThanhToanRequest;
import com.be.server.core.admin.banhang.model.request.ADThemGioHangRequest;
import com.be.server.core.admin.banhang.model.request.ADThemKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.ADThemSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ADXoaSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ChonPhieuGiamGiaKoDuRequest;
import com.be.server.core.admin.banhang.model.request.ChonPhieuGiamGiaRequest;
import com.be.server.core.admin.banhang.model.request.ListKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.ListSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ThemMoiKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.giaoHangRequest;
import com.be.server.core.admin.banhang.model.response.ADChonKhachHangRespones;
import com.be.server.core.admin.banhang.model.response.ADGioHangRespones;
import com.be.server.core.admin.banhang.model.response.ADPhuongThucThanhToanRespones;
import com.be.server.core.admin.banhang.model.response.ADThanhToanRespones;
import com.be.server.core.admin.banhang.model.response.AvailableVouchersResponse;
import com.be.server.core.admin.banhang.model.response.ListHoaDon;
import com.be.server.core.admin.banhang.service.ADBanHangService;
import com.be.server.core.admin.banhang.service.impl.PhieuGiamGiaService;
import com.be.server.core.admin.khachhang.model.request.ADKhachHangRequest;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.HoaDon;
import com.be.server.entity.KhachHang;
import com.be.server.entity.PhieuGiamGia;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_BAN_HANG)
@Slf4j
@CrossOrigin(origins = "*")
public class ADBanHangController {

    public final ADBanHangService adBanHangService;

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping("/danh-sach-phieu-giam-gia-ko_du")
    public ResponseEntity<AvailableVouchersResponse> getAvailableVouchers (ChonPhieuGiamGiaKoDuRequest chonPhieuGiamGiaKoDuRequest) {
        AvailableVouchersResponse response = phieuGiamGiaService.getAvailableVouchers(chonPhieuGiamGiaKoDuRequest.getIdHD(), chonPhieuGiamGiaKoDuRequest.getIdKH(), chonPhieuGiamGiaKoDuRequest.getTongTien());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-hoa-don")
    public List<ListHoaDon> getListHoaDon() {
        return adBanHangService.getHoaDon();
    }

    @PostMapping("/create-hoa-don")
    public ResponseEntity<?> createHoaDon(ADNhanVienRequest adNhanVienRequest) {
        return Helper.createResponseEntity(adBanHangService.createHoaDon(adNhanVienRequest));
    }

    @PostMapping("/huy")
    public ResponseEntity<?> huyHoaDon(ADHuyRequest adNhanVienRequest) {
        return Helper.createResponseEntity(adBanHangService.huy(adNhanVienRequest));
    }

    @PostMapping("them-san-pham")
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADThemSanPhamRequest request) {
        return Helper.createResponseEntity(adBanHangService.createThemSanPham(request));
    }

    @GetMapping("/list-gio-hang/{id}")
    public List<ADGioHangRespones> getListGioHang(@PathVariable("id") String id) {
        return adBanHangService.getListGioHang(id);
    }

    @PostMapping("/xoa-san-pham")
    public void getXoaSP(ADXoaSanPhamRequest id) {
         adBanHangService.xoaHoaDonChiTiet(id);
    }

    @PostMapping("/them-so-luong")
    public ResponseEntity<?> getThemSL(ADXoaSanPhamRequest id) {
        return Helper.createResponseEntity(adBanHangService.ThemSoLuong(id));
    }

    @PostMapping("/them-moi-khach-hang")
    public ResponseEntity<?> themMoiKhachHang(ThemMoiKhachHangRequest id) {
        return Helper.createResponseEntity(adBanHangService.themMoiKhachHang(id));
    }

    @PostMapping("/xoa-so-luong")
    public void getXoaSL(ADXoaSanPhamRequest id) {
        adBanHangService.xoaSoLuong(id);
    }

    @GetMapping("/list-khach-hang")
    public ResponseEntity<?> getListKhachHang(ListKhachHangRequest request) {
      return Helper.createResponseEntity(adBanHangService.listKhachHang(request));
    }

    @GetMapping("/list-san-pham")
    public ResponseEntity<?> getALl(ListSanPhamRequest request) {

        return Helper.createResponseEntity(adBanHangService.getAll(request));
    }

    @PostMapping("/them-khach-hang")
    public void getListKhachHang(ADThemKhachHangRequest id) {
         adBanHangService.themKhachHang(id);
    }

    @GetMapping("/khach-hang/{id}")
    public ADChonKhachHangRespones getKhachHang(@PathVariable("id") String id) {
      return adBanHangService.getKhachHang(id);
    }

    @GetMapping("/thanh-toan/{id}")
    public ADThanhToanRespones getThanhToan(@PathVariable("id") String id) {
        return adBanHangService.getThanhToan(id);
    }

    @GetMapping("/phuong-thuc-thanh-toan/{id}")
    public  List<ADPhuongThucThanhToanRespones>  getPhuongThucThanhToan(@PathVariable("id") String id) {
        return adBanHangService.getPhuongThucThanhToan(id);
    }

    @PostMapping("/cap-nhat-phuong-thuc-thanh-toan")
    public void capNhatPhuongThucThanhToan(ADCapNhatPhuongTHucThanhToanRequest id) {
        adBanHangService.capNhatPhuongThucThanhToan(id);
    }

    @PostMapping("/thanh-toan-thanh-cong")
    public ResponseEntity<?>  thanhToanThanhCong(ADThanhToanRequest id) {
        return Helper.createResponseEntity(adBanHangService.thanhToanThanhCong(id));
    }

    @GetMapping("/danh-sach-phieu-giam-gia")
    public ResponseEntity<?> getDiscountCoupons(ChonPhieuGiamGiaRequest request) {
        return Helper.createResponseEntity(adBanHangService.danhSachPhieuGiamGia(request));

    }

    @PostMapping("/giao-hang/{id}")
    public ResponseEntity<?> getGiaoHang(@PathVariable("id") String id) {
        return Helper.createResponseEntity(adBanHangService.giaoHang(id));

    }

}
