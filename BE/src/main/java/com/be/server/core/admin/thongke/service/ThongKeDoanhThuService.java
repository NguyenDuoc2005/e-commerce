package com.be.server.core.admin.thongke.service;

import com.be.server.core.admin.thongke.model.response.ThongKeDoanhThuResponse;
import com.be.server.core.admin.thongke.model.response.ThongKeDonHangResponse;
import com.be.server.core.admin.thongke.model.response.ThongKeTrangThaiHoaDonResponse;
import com.be.server.core.admin.thongke.model.response.TopSanPhamBanChayResponse;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ThongKeDoanhThuService {

    private final HoaDonRepository hoaDonRepository;

    public ThongKeDoanhThuResponse getThongKeDoanhThu() {
        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();

        // Tính thời gian đầu và cuối tháng này
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // Tính thời gian đầu và cuối ngày hôm nay
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        ZoneOffset zone = ZoneOffset.ofHours(7);
        Long startOfMonthTimestamp = startOfMonth.toEpochSecond(zone) * 1000;
        Long endOfMonthTimestamp = endOfMonth.toEpochSecond(zone) * 1000;
        Long startOfDayTimestamp = startOfDay.toEpochSecond(zone) * 1000;
        Long endOfDayTimestamp = endOfDay.toEpochSecond(zone) * 1000;


        // Log để debug
//        System.out.println("Start of month: " + startOfMonthTimestamp);
//        System.out.println("End of month: " + endOfMonthTimestamp);
//        System.out.println("Start of day: " + startOfDayTimestamp);
//        System.out.println("End of day: " + endOfDayTimestamp);

        // Test dữ liệu
//        System.out.println("Total hoa don count: " + hoaDonRepository.getTotalHoaDonCount());

        // Gọi các query
        Double doanhSoThangNay = hoaDonRepository.getDoanhSoThangNay(startOfMonthTimestamp, endOfMonthTimestamp);
        Integer soHoaDonThangNay = hoaDonRepository.getSoHoaDonThangNay(startOfMonthTimestamp, endOfMonthTimestamp);
        Double doanhSoHomNay = hoaDonRepository.getDoanhSoHomNay(startOfDayTimestamp, endOfDayTimestamp);
        Integer soHoaDonHomNay = hoaDonRepository.getSoHoaDonHomNay(startOfDayTimestamp, endOfDayTimestamp);
        Integer hangBanDuocThangNay = hoaDonRepository.getHangBanDuocThangNay(startOfMonthTimestamp, endOfMonthTimestamp);

        return new ThongKeDoanhThuResponse(
                doanhSoThangNay != null ? doanhSoThangNay : 0.0,
                soHoaDonThangNay != null ? soHoaDonThangNay : 0,
                doanhSoHomNay != null ? doanhSoHomNay : 0.0,
                soHoaDonHomNay != null ? soHoaDonHomNay : 0,
                hangBanDuocThangNay != null ? hangBanDuocThangNay : 0
        );
    }

    public List<ThongKeDonHangResponse> thongKeDonHangHoanThanhTheoKhoangThoiGian(Long startDate, Long endDate) {
        // Lấy dữ liệu từ database
        List<Object[]> ketQuaQuery = hoaDonRepository.thongKeDonHangHoanThanhTheoNgay(startDate, endDate);

        // Chuyển đổi kết quả thành Map để dễ tra cứu
        Map<String, Long> duLieuThongKe = ketQuaQuery.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],  // date
                        row -> ((Number) row[1]).longValue()  // soLuongDonHang
                ));

        // Tạo danh sách kết quả với tất cả các ngày trong khoảng thời gian
        List<ThongKeDonHangResponse> danhSachKetQua = new ArrayList<>();

        // Chuyển đổi timestamp thành LocalDate
        LocalDate ngayBatDau = java.time.Instant.ofEpochMilli(startDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        LocalDate ngayKetThuc = java.time.Instant.ofEpochMilli(endDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Lặp qua từng ngày trong khoảng thời gian
        LocalDate ngayHienTai = ngayBatDau;
        while (!ngayHienTai.isAfter(ngayKetThuc)) {
            String ngayFormatted = ngayHienTai.format(formatter);
            Long soLuongDonHang = duLieuThongKe.getOrDefault(ngayFormatted, 0L);

            danhSachKetQua.add(new ThongKeDonHangResponse(ngayFormatted, soLuongDonHang));
            ngayHienTai = ngayHienTai.plusDays(1);
        }

        return danhSachKetQua;
    }

    public List<TopSanPhamBanChayResponse> layTop3SanPhamBanChay(Long startDate, Long endDate) {
        List<Object[]> ketQuaQuery = hoaDonRepository.layTop3SanPhamBanChay(startDate, endDate);

        List<TopSanPhamBanChayResponse> danhSachKetQua = new ArrayList<>();

        for (Object[] row : ketQuaQuery) {
            TopSanPhamBanChayResponse response = new TopSanPhamBanChayResponse();
            response.setId((String) row[0]);
            response.setMaSanPham((String) row[1]);
            response.setTenSanPham((String) row[2]);
            response.setAnhSanPham((String) row[3]);
            response.setSoLuongBan(((Number) row[4]).longValue());
            Number doanhThuValue = (Number) row[5];
            response.setDoanhThu(doanhThuValue != null ? doanhThuValue.doubleValue() : 0.0);
            response.setThuongHieu((String) row[6]);
            response.setGiaBan((Double) row[7]);

            danhSachKetQua.add(response);
        }

        return danhSachKetQua;
    }

    public List<ThongKeTrangThaiHoaDonResponse> thongKeTiLeTrangThaiHoaDon(
            Long startDate, Long endDate) {

        // Lấy tổng số hóa đơn trong khoảng thời gian
        long total = hoaDonRepository.countTotalHoaDonInPeriod(startDate, endDate);

        // Lấy số lượng theo từng trạng thái
        List<Object[]> statusCounts = hoaDonRepository.countHoaDonByTrangThaiInPeriod(startDate, endDate);

        // Chuyển sang map để dễ tra cứu
        Map<Integer, Long> statusMap = statusCounts.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> ((Number) row[1]).longValue()
                ));

        // Tạo danh sách kết quả
        List<ThongKeTrangThaiHoaDonResponse> results = new ArrayList<>();

        // Duyệt qua tất cả các trạng thái trong enum
        for (EntityTrangThaiHoaDon trangThai : EntityTrangThaiHoaDon.values()) {
            long count = statusMap.getOrDefault(trangThai.ordinal(), 0L);
            double percentage = total > 0 ? (count * 100.0 / total) : 0;

            results.add(new ThongKeTrangThaiHoaDonResponse(
                    trangThai.name(),
                    count,
                    percentage
            ));
        }

        return results;
    }

}
