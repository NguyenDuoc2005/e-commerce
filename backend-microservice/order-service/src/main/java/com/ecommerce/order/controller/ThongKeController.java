package com.ecommerce.order.controller;

import com.ecommerce.order.model.response.ThongKeDoanhThuResponse;
import com.ecommerce.order.model.response.ThongKeDonHangResponse;
import com.ecommerce.order.model.response.OrderStatusStatisticsResponse;
import com.ecommerce.order.model.response.TopSellingProductResponse;
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
import java.util.Map;

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
            @RequestParam("startDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate
    ) {
        Long start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ResponseEntity.ok(service.thongKeDonHangHoanThanhTheoKhoangThoiGian(start, end));
    }

    @GetMapping("/top-san-pham-ban-chay")
    public ResponseEntity<List<TopSellingProductResponse>> layTop3ProductBanChay(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate
    ) {
        Long start = startDate == null ? null : startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long end = endDate == null ? null : endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ResponseEntity.ok(service.layTop3ProductBanChay(start, end));
    }

    @GetMapping("/ti-le-trang-thai")
    public ResponseEntity<List<OrderStatusStatisticsResponse>> thongKeTiLeTrangThaiOrder(
            @RequestParam("startDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate
    ) {
        Long start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ResponseEntity.ok(service.thongKeTiLeTrangThaiOrder(start, end));
    }

    @GetMapping("/marketplace-dashboard")
    public ResponseEntity<Map<String, Object>> marketplaceDashboard() {
        return ResponseEntity.ok(service.marketplaceDashboard());
    }
}
