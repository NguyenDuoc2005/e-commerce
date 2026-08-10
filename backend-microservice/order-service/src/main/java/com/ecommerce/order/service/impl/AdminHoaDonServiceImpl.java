package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import com.ecommerce.order.model.request.ChangeStatusRequest;
import com.ecommerce.order.model.request.HoaDonDetailRequest;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.ThanhToanRequest;
import com.ecommerce.order.model.response.HoaDonPageResponse;
import com.ecommerce.order.model.response.HoaDonResponse;
import com.ecommerce.order.service.AdminHoaDonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminHoaDonServiceImpl implements AdminHoaDonService {

    private static final int LUU_TAM = EntityTrangThaiHoaDon.LUU_TAM.ordinal();

    private final JdbcTemplate jdbcTemplate;

    public AdminHoaDonServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResponseObject<?> getAllHoaDon(HoaDonSearchRequest request) {
        try {
            Pageable pageable = pageable(request.getPage(), request.getSize());
            String q = like(request.getQ());
            Integer status = request.getStatus() == null ? null : request.getStatus().ordinal();
            String where = """
                    WHERE (? IS NULL OR ? = ''
                        OR LOWER(hd.ten_khach_hang) LIKE LOWER(?)
                        OR LOWER(hd.ma_hoa_don) LIKE LOWER(?)
                        OR LOWER(hd.so_dien_thoai_khach_hang) LIKE LOWER(?)
                        OR LOWER(hd.id_nhan_vien) LIKE LOWER(?))
                      AND (? IS NULL OR hd.trang_thai_hoa_don = ?)
                      AND (? IS NULL OR hd.created_date >= ?)
                      AND (? IS NULL OR hd.created_date <= ?)
                      AND hd.trang_thai_hoa_don != ?
                    """;

            List<HoaDonResponse> rows = jdbcTemplate.query("""
                            SELECT hd.id,
                                   hd.ma_hoa_don,
                                   hd.ten_khach_hang,
                                   hd.so_dien_thoai_khach_hang AS so_dien_thoai,
                                   hd.id_nhan_vien AS ma_nhan_vien,
                                   hd.id_nhan_vien AS ten_nhan_vien,
                                   hd.tong_tien_sau_giam,
                                   hd.loai_hoa_don,
                                   hd.created_date,
                                   hd.trang_thai_hoa_don
                            FROM hoa_don hd
                            """ + where + " ORDER BY hd.created_date ASC LIMIT ? OFFSET ?",
                    (rs, rowNum) -> new HoaDonResponse(
                            rs.getString("id"),
                            rs.getString("ma_hoa_don"),
                            rs.getString("ten_khach_hang"),
                            rs.getString("so_dien_thoai"),
                            rs.getString("ma_nhan_vien"),
                            rs.getString("ten_nhan_vien"),
                            rs.getObject("tong_tien_sau_giam") == null ? null : rs.getDouble("tong_tien_sau_giam"),
                            intOrNull(rs.getObject("loai_hoa_don")),
                            rs.getObject("created_date") == null ? null : rs.getLong("created_date"),
                            intOrNull(rs.getObject("trang_thai_hoa_don"))),
                    q, q, q, q, q, q, status, status, request.getStartDate(), request.getStartDate(),
                    request.getEndDate(), request.getEndDate(), LUU_TAM, pageable.getPageSize(), pageable.getOffset());

            Long total = jdbcTemplate.queryForObject("SELECT COUNT(hd.id) FROM hoa_don hd " + where,
                    Long.class, q, q, q, q, q, q, status, status, request.getStartDate(), request.getStartDate(),
                    request.getEndDate(), request.getEndDate(), LUU_TAM);

            Map<EntityTrangThaiHoaDon, Long> countByStatus = new LinkedHashMap<>();
            jdbcTemplate.query("""
                            SELECT hd.trang_thai_hoa_don, COUNT(hd.id) AS total
                            FROM hoa_don hd
                            WHERE (? IS NULL OR ? = ''
                                OR LOWER(hd.ten_khach_hang) LIKE LOWER(?)
                                OR LOWER(hd.so_dien_thoai_khach_hang) LIKE LOWER(?)
                                OR LOWER(hd.id_nhan_vien) LIKE LOWER(?))
                              AND (? IS NULL OR hd.created_date >= ?)
                              AND (? IS NULL OR hd.created_date <= ?)
                              AND hd.trang_thai_hoa_don != ?
                            GROUP BY hd.trang_thai_hoa_don
                            """,
                    rs -> {
                        Integer ordinal = intOrNull(rs.getObject("trang_thai_hoa_don"));
                        if (ordinal != null && ordinal >= 0 && ordinal < EntityTrangThaiHoaDon.values().length) {
                            countByStatus.put(EntityTrangThaiHoaDon.values()[ordinal], rs.getLong("total"));
                        }
                    },
                    q, q, q, q, q, request.getStartDate(), request.getStartDate(), request.getEndDate(), request.getEndDate(), LUU_TAM);

            Page<HoaDonResponse> page = new PageImpl<>(rows, pageable, total == null ? 0 : total);
            return new ResponseObject<>(new HoaDonPageResponse(page, countByStatus), HttpStatus.OK, "Lay danh sach hoa don thanh cong");
        } catch (Exception e) {
            return new ResponseObject<>(null, HttpStatus.INTERNAL_SERVER_ERROR, "Loi khi lay danh sach hoa don: " + e.getMessage());
        }
    }

    @Override
    public ResponseObject<?> getAllHoaDonChiTiet(HoaDonDetailRequest request) {
        try {
            Pageable pageable = pageable(request.getPage(), request.getSize());
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(detailSql() + " LIMIT ? OFFSET ?", request.getMaHoaDon(), pageable.getPageSize(), pageable.getOffset());
            Long total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM hoa_don_chi_tiet hdct
                    LEFT JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
                    WHERE hd.ma_hoa_don = ?
                    """, Long.class, request.getMaHoaDon());
            Page<Map<String, Object>> page = new PageImpl<>(rows, pageable, total == null ? 0 : total);
            return new ResponseObject<>(page, HttpStatus.OK, "Lay danh sach chi tiet hoa don thanh cong");
        } catch (Exception e) {
            return new ResponseObject<>(null, HttpStatus.INTERNAL_SERVER_ERROR, "Loi khi lay chi tiet hoa don: " + e.getMessage());
        }
    }

    @Override
    public ResponseObject<?> getLichSuTrangThai(String hoaDonId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT trang_thai AS trangThai, thoi_gian AS thoiGian, note AS note
                FROM lich_su_trang_thai_hoa_don
                WHERE hoa_don_id = ?
                ORDER BY thoi_gian DESC
                """, hoaDonId);
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay danh sach lich su trang thai hoa don thanh cong");
    }

    @Override
    public ResponseObject<?> getLichSuThanhToan(String hoaDonId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT ROW_NUMBER() OVER (ORDER BY l.thoi_gian DESC) AS stt,
                       l.so_tien AS soTien,
                       l.thoi_gian AS thoiGian,
                       l.ma_giao_dich AS maGiaoDich,
                       l.loai_giao_dich AS loaiGiaoDich,
                       l.ghi_chu AS ghiChu,
                       l.nhan_vien_id AS tenNhanVien,
                       l.hoa_don_id AS hoaDonId
                FROM lich_su_thanh_toan l
                WHERE l.hoa_don_id = ?
                ORDER BY l.thoi_gian DESC
                """, hoaDonId);
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay danh sach lich su thanh toan hoa don thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> thanhToanHoaDon(ThanhToanRequest request) {
        int hoaDonCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hoa_don WHERE id = ?", Integer.class, request.getHoaDonId());
        if (hoaDonCount == 0) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Hoa don khong ton tai");
        }
        double soTien = value(request.getSoTienKhachDua()) - value(request.getSoTienTraLai());
        jdbcTemplate.update("""
                INSERT INTO lich_su_thanh_toan (so_tien, thoi_gian, ma_giao_dich, loai_giao_dich, nhan_vien_id, hoa_don_id, ghi_chu)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, soTien, LocalDateTime.now(), UUID.randomUUID().toString(), request.getLoaiGiaoDich(), request.getNhanVienId(), request.getHoaDonId(), request.getGhiChu());
        jdbcTemplate.update("""
                UPDATE hoa_don
                SET tong_tien_sau_giam = ?, tong_tien = ?, trang_thai_hoa_don = ?
                WHERE id = ?
                """, soTien, request.getSoTienGoc(), request.getTrangThai() == null ? null : request.getTrangThai().ordinal(), request.getHoaDonId());
        return new ResponseObject<>(Map.of("id", request.getHoaDonId()), HttpStatus.OK, "Thanh toan thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> changeStatus(ChangeStatusRequest request) {
        Map<String, Object> hoaDon = jdbcTemplate.queryForMap("SELECT id, id_voucher FROM hoa_don WHERE ma_hoa_don = ?", request.getMaHoaDon());
        String hoaDonId = String.valueOf(hoaDon.get("id"));
        jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hoa_don = ? WHERE ma_hoa_don = ?", request.getStatus().ordinal(), request.getMaHoaDon());
        jdbcTemplate.update("""
                INSERT INTO lich_su_trang_thai_hoa_don (hoa_don_id, trang_thai, thoi_gian, note)
                VALUES (?, ?, ?, ?)
                """, hoaDonId, request.getStatus().ordinal(), LocalDateTime.now(), request.getNote());

        if (request.getStatus() == EntityTrangThaiHoaDon.DA_HUY) {
            // Inventory and voucher compensation belong to inventory/promotion services after the split.
            // Keep admin invoice status usable without issuing cross-service table updates from order DB.
        }

        return new ResponseObject<>(Map.of("maHoaDon", request.getMaHoaDon()), HttpStatus.OK, "Thay doi thanh cong");
    }

    @Override
    public byte[] generateInvoicePdf(String maHoaDon) {
        return minimalPdf("Hoa don " + maHoaDon);
    }

    @Override
    public byte[] generateDeliveryPdf(String maHoaDon) {
        return minimalPdf("Phieu giao hang " + maHoaDon);
    }

    private static Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page - 1, 0), size <= 0 ? 10 : size);
    }

    private static String like(String q) {
        if (q == null || q.trim().isEmpty()) {
            return "";
        }
        return "%" + q.trim() + "%";
    }

    private static Integer intOrNull(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private static int intValue(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static double value(Double value) {
        return value == null ? 0D : value;
    }

    private static String detailSql() {
        return """
                SELECT hd.ma_hoa_don AS maHoaDon,
                       hd.ma_hoa_don AS tenHoaDon,
                       hdct.ma_hoa_don_chi_tiet AS maHoaDonChiTiet,
                       COALESCE(hdct.ten_hoa_don_chi_tiet, hdct.id_spct) AS tenSanPham,
                       NULL AS anhSanPham,
                       NULL AS thuongHieu,
                       NULL AS xuatSu,
                       NULL AS mauSac,
                       NULL AS size,
                       hdct.so_luong AS soLuong,
                       hdct.gia_ban AS giaBan,
                       (hdct.gia_ban * hdct.so_luong) AS thanhTienSP,
                       (SELECT SUM(hdsub.so_luong * hdsub.gia_ban) FROM hoa_don_chi_tiet hdsub WHERE hdsub.id_hoa_don = hd.id) AS thanhTien,
                       hd.ten_khach_hang AS tenKhachHang2,
                       hd.so_dien_thoai_khach_hang AS sdtKH2,
                       hd.email AS email2,
                       hd.dia_chi_giao_hang AS diaChi2,
                       hd.ten_khach_hang AS tenKhachHang,
                       hd.so_dien_thoai_khach_hang AS sdtKH,
                       hd.email AS email,
                       hd.dia_chi_giao_hang AS diaChi,
                       hd.loai_hoa_don AS loaiHoaDon,
                       hd.trang_thai_hoa_don AS trangThaiHoaDon,
                       NULL AS thoiGian,
                       hd.created_date AS ngayTao,
                       hd.phi_van_chuyen AS phiVanChuyen,
                       hd.id_voucher AS maVoucher,
                       hd.id_voucher AS tenVoucher,
                       hd.giam_gia AS giaTriVoucher,
                       hd.tong_tien_sau_giam AS tongTienSauGiam,
                       (hdct.gia_ban * hdct.so_luong) AS tongTien,
                       hd.phuong_thuc_thanh_toan AS phuongThucThanhToan,
                       hd.du_no AS duNo,
                       hd.hoan_phi AS hoanPhi
                FROM hoa_don_chi_tiet hdct
                LEFT JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
                WHERE hd.ma_hoa_don = ?
                """;
    }

    private static byte[] minimalPdf(String text) {
        String escaped = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n"
                + "4 0 obj << /Length " + (44 + escaped.length()) + " >> stream\nBT /F1 16 Tf 72 760 Td (" + escaped + ") Tj ET\nendstream endobj\n"
                + "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "trailer << /Root 1 0 R >>\n%%EOF";
        return pdf.getBytes(StandardCharsets.US_ASCII);
    }
}
