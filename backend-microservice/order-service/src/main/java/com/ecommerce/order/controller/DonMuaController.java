package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.ChangeStatusRequest;
import com.ecommerce.order.model.request.HoaDonDetailRequest;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.SanPhamChiTietSearchRequest;
import com.ecommerce.order.model.request.ThemSanPhamRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;
import com.ecommerce.order.service.AdminHoaDonService;
import com.ecommerce.order.service.DonMuaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permitall/don-mua")
public class DonMuaController {

    private final DonMuaService donMuaService;
    private final AdminHoaDonService adminHoaDonService;

    public DonMuaController(DonMuaService donMuaService, AdminHoaDonService adminHoaDonService) {
        this.donMuaService = donMuaService;
        this.adminHoaDonService = adminHoaDonService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute HoaDonSearchRequest request) {
        return ResponseEntity.ok(donMuaService.getAllHoaDon(request));
    }

    @GetMapping("/spct")
    public ResponseEntity<?> getAllSanPhamChiTiet(@ModelAttribute SanPhamChiTietSearchRequest request) {
        return ResponseEntity.ok(donMuaService.getAllSanPhamChiTiet(request));
    }

    @GetMapping("/all/{code}")
    public ResponseEntity<?> getAllByCode(@PathVariable String code) {
        return ResponseEntity.ok(donMuaService.getAllHoaDonByCode(code));
    }

    @PostMapping("/sua-thong-tin")
    public ResponseEntity<?> suaThongTin(@ModelAttribute UpdateDeliveryRequest request) {
        return ResponseEntity.ok(donMuaService.suaThongTin(request));
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatus(@ModelAttribute ChangeStatusRequest request) {
        return ResponseEntity.ok(adminHoaDonService.changeStatus(request));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getHoaDonChiTiet(@ModelAttribute HoaDonDetailRequest request) {
        return ResponseEntity.ok(donMuaService.getHoaDonChiTiet(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLichSuHoaDon(@PathVariable String id) {
        return ResponseEntity.ok(adminHoaDonService.getLichSuTrangThai(id));
    }

    @PostMapping("them-san-pham")
    public ResponseEntity<?> themSanPham(@ModelAttribute ThemSanPhamRequest request) {
        return ResponseEntity.ok(donMuaService.themSanPham(request));
    }

    @GetMapping("/lich_su_thanh_toan/{id}")
    public ResponseEntity<?> getLichSuThanhToan(@PathVariable String id) {
        return ResponseEntity.ok(adminHoaDonService.getLichSuThanhToan(id));
    }
}
