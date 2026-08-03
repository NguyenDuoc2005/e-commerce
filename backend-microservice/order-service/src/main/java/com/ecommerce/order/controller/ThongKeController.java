package com.ecommerce.order.controller;

import com.ecommerce.order.model.response.ThongKeDoanhThuResponse;
import com.ecommerce.order.model.response.ThongKeDonHangResponse;
import com.ecommerce.order.model.response.ThongKeTrangThaiHoaDonResponse;
import com.ecommerce.order.model.response.TopSanPhamBanChayResponse;
import com.ecommerce.order.service.ThongKeDoanhThuService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/thong-ke")
@CrossOrigin(origins = "*")
public class ThongKeController {
    private final ThongKeDoanhThuService service;

    public ThongKeController(ThongKeDoanhThuService service) {
        this.service = service;
    }

    @GetMapping("/doanh-thu")
    public ResponseEntity<ThongKeDoanhThuResponse> getThongKeDoanhThu() {
        return ResponseEntity.ok(service.getThongKeDoanhThu());
    }

    @GetMapping("/don-hang-hoan-thanh")
    public ResponseEntity<List<ThongKeDonHangResponse>> thongKeDonHangHoanThanhTheoKhoangThoiGian(
            @RequestParam("ngayBatDau") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayBatDau,
            @RequestParam("ngayKetThuc") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayKetThuc
    ) {
        Long start = ngayBatDau.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long end = ngayKetThuc.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ResponseEntity.ok(service.thongKeDonHangHoanThanhTheoKhoangThoiGian(start, end));
    }

    @GetMapping("/top-san-pham-ban-chay")
    public ResponseEntity<List<TopSanPhamBanChayResponse>> layTop3SanPhamBanChay(
            @RequestParam(value = "ngayBatDau", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayBatDau,
            @RequestParam(value = "ngayKetThuc", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayKetThuc
    ) {
        Long start = ngayBatDau == null ? null : ngayBatDau.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long end = ngayKetThuc == null ? null : ngayKetThuc.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ResponseEntity.ok(service.layTop3SanPhamBanChay(start, end));
    }

    @GetMapping("/ti-le-trang-thai")
    public ResponseEntity<List<ThongKeTrangThaiHoaDonResponse>> thongKeTiLeTrangThaiHoaDon(
            @RequestParam("ngayBatDau") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayBatDau,
            @RequestParam("ngayKetThuc") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayKetThuc
    ) {
        Long start = ngayBatDau.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long end = ngayKetThuc.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ResponseEntity.ok(service.thongKeTiLeTrangThaiHoaDon(start, end));
    }
}
