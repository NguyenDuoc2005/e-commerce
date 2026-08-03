package com.ecommerce.order.service;

import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import com.ecommerce.order.model.response.ThongKeDoanhThuResponse;
import com.ecommerce.order.model.response.ThongKeDonHangResponse;
import com.ecommerce.order.model.response.ThongKeTrangThaiHoaDonResponse;
import com.ecommerce.order.model.response.TopSanPhamBanChayResponse;
import com.ecommerce.order.repository.HoaDonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ThongKeDoanhThuService {
    private final HoaDonRepository hoaDonRepository;

    public ThongKeDoanhThuService(HoaDonRepository hoaDonRepository) {
        this.hoaDonRepository = hoaDonRepository;
    }

    public ThongKeDoanhThuResponse getThongKeDoanhThu() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        ZoneOffset zone = ZoneOffset.ofHours(7);
        Long startOfMonthTs = startOfMonth.toEpochSecond(zone) * 1000;
        Long endOfMonthTs = endOfMonth.toEpochSecond(zone) * 1000;
        Long startOfDayTs = startOfDay.toEpochSecond(zone) * 1000;
        Long endOfDayTs = endOfDay.toEpochSecond(zone) * 1000;
        return new ThongKeDoanhThuResponse(
                defaultDouble(hoaDonRepository.getDoanhSoThangNay(startOfMonthTs, endOfMonthTs)),
                defaultInteger(hoaDonRepository.getSoHoaDonThangNay(startOfMonthTs, endOfMonthTs)),
                defaultDouble(hoaDonRepository.getDoanhSoHomNay(startOfDayTs, endOfDayTs)),
                defaultInteger(hoaDonRepository.getSoHoaDonHomNay(startOfDayTs, endOfDayTs)),
                defaultInteger(hoaDonRepository.getHangBanDuocThangNay(startOfMonthTs, endOfMonthTs))
        );
    }

    public List<ThongKeDonHangResponse> thongKeDonHangHoanThanhTheoKhoangThoiGian(Long startDate, Long endDate) {
        Map<String, Long> data = hoaDonRepository.thongKeDonHangHoanThanhTheoNgay(startDate, endDate).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).longValue()));
        List<ThongKeDonHangResponse> result = new ArrayList<>();
        LocalDate start = java.time.Instant.ofEpochMilli(startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate end = java.time.Instant.ofEpochMilli(endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
            String formatted = current.format(formatter);
            result.add(new ThongKeDonHangResponse(formatted, data.getOrDefault(formatted, 0L)));
        }
        return result;
    }

    public List<TopSanPhamBanChayResponse> layTop3SanPhamBanChay(Long startDate, Long endDate) {
        List<TopSanPhamBanChayResponse> result = new ArrayList<>();
        for (Object[] row : hoaDonRepository.layTop3SanPhamBanChay(startDate, endDate)) {
            TopSanPhamBanChayResponse response = new TopSanPhamBanChayResponse();
            response.setId((String) row[0]);
            response.setMaSanPham((String) row[1]);
            response.setTenSanPham((String) row[2]);
            response.setAnhSanPham((String) row[3]);
            response.setSoLuongBan(((Number) row[4]).longValue());
            response.setDoanhThu(row[5] == null ? 0.0 : ((Number) row[5]).doubleValue());
            response.setThuongHieu((String) row[6]);
            response.setGiaBan(row[7] == null ? 0.0 : ((Number) row[7]).doubleValue());
            result.add(response);
        }
        return result;
    }

    public List<ThongKeTrangThaiHoaDonResponse> thongKeTiLeTrangThaiHoaDon(Long startDate, Long endDate) {
        long total = hoaDonRepository.countTotalHoaDonInPeriod(startDate, endDate);
        Map<Integer, Long> statusMap = hoaDonRepository.countHoaDonByTrangThaiInPeriod(startDate, endDate).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));
        List<ThongKeTrangThaiHoaDonResponse> result = new ArrayList<>();
        for (EntityTrangThaiHoaDon status : EntityTrangThaiHoaDon.values()) {
            long count = statusMap.getOrDefault(status.ordinal(), 0L);
            result.add(new ThongKeTrangThaiHoaDonResponse(status.name(), count, total > 0 ? (count * 100.0 / total) : 0));
        }
        return result;
    }

    private Double defaultDouble(Double value) { return value == null ? 0.0 : value; }
    private Integer defaultInteger(Integer value) { return value == null ? 0 : value; }
}
