package com.be.server.core.admin.thongke.controller;

import com.be.server.core.admin.thongke.model.response.ThongKeDoanhThuResponse;
import com.be.server.core.admin.thongke.model.response.ThongKeDonHangResponse;
import com.be.server.core.admin.thongke.model.response.ThongKeTrangThaiHoaDonResponse;
import com.be.server.core.admin.thongke.model.response.TopSanPhamBanChayResponse;
import com.be.server.core.admin.thongke.service.ThongKeDoanhThuService;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.infrastructure.constant.MappingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_THONG_KE)
@Slf4j
@CrossOrigin(origins = "*")
public class ADThongKeController {

    private final ThongKeDoanhThuService thongKeDoanhThuService;

    @GetMapping("/doanh-thu")
    public ResponseEntity<ThongKeDoanhThuResponse> getThongKeDoanhThu() {
        ThongKeDoanhThuResponse thongKe = thongKeDoanhThuService.getThongKeDoanhThu();
        return ResponseEntity.ok(thongKe);
    }

    @GetMapping("/don-hang-hoan-thanh")
    public ResponseEntity<List<ThongKeDonHangResponse>> thongKeDonHangHoanThanhTheoKhoangThoiGian(
            @RequestParam("ngayBatDau") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayBatDau,
            @RequestParam("ngayKetThuc") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayKetThuc) {

        // Chuyển đổi LocalDate thành timestamp (Long)
        Long startTimestamp = ngayBatDau.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long endTimestamp = ngayKetThuc.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        List<ThongKeDonHangResponse> ketQua = thongKeDoanhThuService.thongKeDonHangHoanThanhTheoKhoangThoiGian(startTimestamp, endTimestamp);

        return ResponseEntity.ok(ketQua);
    }

    @GetMapping("/top-san-pham-ban-chay")
    public ResponseEntity<List<TopSanPhamBanChayResponse>> layTop3SanPhamBanChay(
            @RequestParam(value = "ngayBatDau", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayBatDau,
            @RequestParam(value = "ngayKetThuc", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayKetThuc) {

        Long startTimestamp = null;
        Long endTimestamp = null;

        if (ngayBatDau != null) {
            startTimestamp = ngayBatDau.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        if (ngayKetThuc != null) {
            endTimestamp = ngayKetThuc.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        List<TopSanPhamBanChayResponse> ketQua = thongKeDoanhThuService.layTop3SanPhamBanChay(startTimestamp, endTimestamp);

        return ResponseEntity.ok(ketQua);
    }

    @GetMapping("/ti-le-trang-thai")
    public ResponseEntity<List<ThongKeTrangThaiHoaDonResponse>> thongKeTiLeTrangThaiHoaDon(
            @RequestParam("ngayBatDau") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayBatDau,
            @RequestParam("ngayKetThuc") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate ngayKetThuc) {

        Long startTimestamp = ngayBatDau.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long endTimestamp = ngayKetThuc.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        List<ThongKeTrangThaiHoaDonResponse> ketQua =
                thongKeDoanhThuService.thongKeTiLeTrangThaiHoaDon(startTimestamp, endTimestamp);

        return ResponseEntity.ok(ketQua);
    }

}

