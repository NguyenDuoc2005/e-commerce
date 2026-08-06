package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.BanHangRequest;
import com.ecommerce.order.service.BanHangService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/admin/ban-hang")
public class BanHangController {

    private final BanHangService banHangService;
    private final ObjectMapper objectMapper;

    public BanHangController(BanHangService banHangService, ObjectMapper objectMapper) {
        this.banHangService = banHangService;
        this.objectMapper = objectMapper;
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
    public ResponseEntity<?> createHoaDon(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        return ResponseEntity.ok(banHangService.createHoaDon(resolveRequest(request, servletRequest)));
    }

    @PostMapping("/huy")
    public ResponseEntity<?> huyHoaDon(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        return ResponseEntity.ok(banHangService.huy(resolveRequest(request, servletRequest)));
    }

    @PostMapping("them-san-pham")
    public ResponseEntity<?> modifyProduct(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        return ResponseEntity.ok(banHangService.themSanPham(resolveRequest(request, servletRequest)));
    }

    @GetMapping("/list-gio-hang/{id}")
    public ResponseEntity<?> getListGioHang(@PathVariable String id) {
        return ResponseEntity.ok(banHangService.getListGioHang(id));
    }

    @PostMapping("/xoa-san-pham")
    public ResponseEntity<?> xoaSanPham(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        banHangService.xoaSanPham(resolveRequest(request, servletRequest));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/them-so-luong")
    public ResponseEntity<?> themSoLuong(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        return ResponseEntity.ok(banHangService.themSoLuong(resolveRequest(request, servletRequest)));
    }

    @PostMapping("/them-moi-khach-hang")
    public ResponseEntity<?> themMoiKhachHang(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        return ResponseEntity.ok(banHangService.themMoiKhachHang(resolveRequest(request, servletRequest)));
    }

    @PostMapping("/xoa-so-luong")
    public ResponseEntity<?> xoaSoLuong(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        banHangService.xoaSoLuong(resolveRequest(request, servletRequest));
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
    public ResponseEntity<?> themKhachHang(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        banHangService.themKhachHang(resolveRequest(request, servletRequest));
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
    public ResponseEntity<?> capNhatPhuongThucThanhToan(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        banHangService.capNhatPhuongThucThanhToan(resolveRequest(request, servletRequest));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/thanh-toan-thanh-cong")
    public ResponseEntity<?> thanhToanThanhCong(@ModelAttribute BanHangRequest request, HttpServletRequest servletRequest) throws IOException {
        return ResponseEntity.ok(banHangService.thanhToanThanhCong(resolveRequest(request, servletRequest)));
    }

    @GetMapping("/danh-sach-phieu-giam-gia")
    public ResponseEntity<?> getDiscountCoupons(@ModelAttribute BanHangRequest request) {
        return ResponseEntity.ok(banHangService.danhSachPhieuGiamGia(request));
    }

    @PostMapping("/giao-hang/{id}")
    public ResponseEntity<?> giaoHang(@PathVariable String id) {
        return ResponseEntity.ok(banHangService.giaoHang(id));
    }

    private BanHangRequest resolveRequest(BanHangRequest formRequest, HttpServletRequest servletRequest) throws IOException {
        String contentType = servletRequest.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            BanHangRequest bodyRequest = objectMapper.readValue(servletRequest.getInputStream(), BanHangRequest.class);
            return bodyRequest == null ? formRequest : bodyRequest;
        }
        return formRequest;
    }
}
