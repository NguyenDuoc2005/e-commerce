package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.BanHangRequest;
import com.ecommerce.order.service.BanHangService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ban-hang")
public class BanHangController {

    private final BanHangService banHangService;

    public BanHangController(BanHangService banHangService) {
        this.banHangService = banHangService;
    }

    @GetMapping("/danh-sach-phieu-giam-gia-ko_du")
    public ResponseEntity<?> getAvailableVouchers(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.availableVouchers(request.getIdHD(), request.getIdKH(), request.getTongTien()));
    }

    @GetMapping("/list-hoa-don")
    public ResponseEntity<?> getListHoaDon() {
        return ResponseEntity.ok(banHangService.getHoaDon());
    }

    @PostMapping("/create-hoa-don")
    public ResponseEntity<?> createHoaDon(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.createHoaDon(request));
    }

    @PostMapping("/huy")
    public ResponseEntity<?> huyHoaDon(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.huy(request));
    }

    @PostMapping("them-san-pham")
    public ResponseEntity<?> modifyProduct(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.themSanPham(request));
    }

    @GetMapping("/list-gio-hang/{id}")
    public ResponseEntity<?> getListGioHang(@PathVariable String id) {
        return ResponseEntity.ok(banHangService.getListGioHang(id));
    }

    @PostMapping("/xoa-san-pham")
    public ResponseEntity<?> xoaSanPham(@ModelAttribute BanHangRequest request) {
        banHangService.xoaSanPham(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/them-so-luong")
    public ResponseEntity<?> themSoLuong(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.themSoLuong(request));
    }

    @PostMapping("/them-moi-khach-hang")
    public ResponseEntity<?> themMoiKhachHang(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.themMoiKhachHang(request));
    }

    @PostMapping("/xoa-so-luong")
    public ResponseEntity<?> xoaSoLuong(@ModelAttribute BanHangRequest request) {
        banHangService.xoaSoLuong(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list-khach-hang")
    public ResponseEntity<?> getListKhachHang(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.listKhachHang(request));
    }

    @GetMapping("/list-san-pham")
    public ResponseEntity<?> getAll(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.getAllSanPham(request));
    }

    @PostMapping("/them-khach-hang")
    public ResponseEntity<?> themKhachHang(@ModelAttribute BanHangRequest request) {
        banHangService.themKhachHang(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/khach-hang/{id}")
    public ResponseEntity<?> getKhachHang(@PathVariable String id) {
        return ResponseEntity.ok(banHangService.getKhachHang(id));
    }

    @GetMapping("/thanh-toan/{id}")
    public ResponseEntity<?> getThanhToan(@PathVariable String id) {
        return ResponseEntity.ok(banHangService.getThanhToan(id));
    }

    @GetMapping("/phuong-thuc-thanh-toan/{id}")
    public ResponseEntity<?> getPhuongThucThanhToan(@PathVariable String id) {
        return ResponseEntity.ok(banHangService.getPhuongThucThanhToan(id));
    }

    @PostMapping("/cap-nhat-phuong-thuc-thanh-toan")
    public ResponseEntity<?> capNhatPhuongThucThanhToan(@ModelAttribute BanHangRequest request) {
        banHangService.capNhatPhuongThucThanhToan(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/thanh-toan-thanh-cong")
    public ResponseEntity<?> thanhToanThanhCong(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.thanhToanThanhCong(request));
    }

    @GetMapping("/danh-sach-phieu-giam-gia")
    public ResponseEntity<?> getDiscountCoupons(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.danhSachPhieuGiamGia(request));
    }

    @PostMapping("/giao-hang/{id}")
    public ResponseEntity<?> giaoHang(@PathVariable String id) {
        return ResponseEntity.ok(banHangService.giaoHang(id));
    }
}
