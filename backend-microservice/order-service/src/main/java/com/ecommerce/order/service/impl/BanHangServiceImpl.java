package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.constant.EntityLoaiHoaDon;
import com.ecommerce.order.constant.EntityPhuongThucThanhToan;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import com.ecommerce.order.model.request.BanHangRequest;
import com.ecommerce.order.service.BanHangService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BanHangServiceImpl implements BanHangService {

    private static final int ACTIVE = 0;

    private final JdbcTemplate jdbcTemplate;

    public BanHangServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> availableVouchers(String idHD, String idKH, Double tongTien) {
        List<Map<String, Object>> vouchers = voucherRows(idKH);
        double total = value(tongTien);
        final double voucherTotal = total;
        List<Map<String, Object>> available = vouchers.stream()
                .filter(v -> intValue(v.get("so_luong_phieu")) > 0 && doubleValue(v.get("dieu_kien")) <= voucherTotal)
                .peek(v -> v.put("giaTriGiamThucTe", discountValue(v, voucherTotal)))
                .toList();
        Map<String, Object> best = available.stream().max(Comparator.comparingDouble(v -> doubleValue(v.get("giaTriGiamThucTe")))).orElse(null);
        Map<String, Object> better = vouchers.stream()
                .peek(v -> v.put("giaTriGiamThucTe", discountValue(v, voucherTotal)))
                .filter(v -> doubleValue(v.get("dieu_kien")) > voucherTotal && doubleValue(v.get("giaTriGiamThucTe")) > (best == null ? 0D : doubleValue(best.get("giaTriGiamThucTe"))))
                .findFirst()
                .map(v -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("ma", v.get("ma_phieu_giam_gia"));
                    row.put("giaTriGiamThucTe", v.get("giaTriGiamThucTe"));
                    row.put("amountNeeded", doubleValue(v.get("dieu_kien")) - voucherTotal);
                    return row;
                })
                .orElse(null);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("availableVouchers", available);
        response.put("bestVoucher", best);
        response.put("betterVoucher", better);
        return response;
    }

    @Override
    public List<Map<String, Object>> getHoaDon() {
        return jdbcTemplate.queryForList("""
                SELECT hd.id AS id, hd.ma_hoa_don AS ma, SUM(hdct.so_luong) AS soLuong, hd.loai_hoa_don AS loaiHoaDon
                FROM hoa_don hd
                LEFT JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
                WHERE hd.trang_thai_hoa_don = ? AND (hd.loai_hoa_don = ? OR hd.loai_hoa_don = ?)
                GROUP BY hd.id, hd.ma_hoa_don, hd.loai_hoa_don
                ORDER BY hd.id ASC
                """, EntityTrangThaiHoaDon.CHO_XAC_NHAN.ordinal(), EntityLoaiHoaDon.OFFLINE.ordinal(), EntityLoaiHoaDon.GIAO_HANG.ordinal());
    }

    @Override
    @Transactional
    public ResponseObject<?> createHoaDon(BanHangRequest request) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                INSERT INTO hoa_don (id, status, created_date, ma_hoa_don, tong_tien, tong_tien_sau_giam, id_nhan_vien, trang_thai_hoa_don, loai_hoa_don)
                VALUES (?, ?, ?, ?, 0, 0, ?, ?, ?)
                """, id, ACTIVE, System.currentTimeMillis(), generateCode("HD"), request.getIdNV(),
                EntityTrangThaiHoaDon.CHO_XAC_NHAN.ordinal(), EntityLoaiHoaDon.OFFLINE.ordinal());
        insertStatusHistory(id, EntityTrangThaiHoaDon.CHO_XAC_NHAN.ordinal(), "Don hang da duoc tao va dang cho xu ly.");
        return new ResponseObject<>(getOrder(id), HttpStatus.CREATED, "Tao hoa don thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> huy(BanHangRequest request) {
        jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hoa_don = ? WHERE id = ?", EntityTrangThaiHoaDon.DA_HUY.ordinal(), request.getIdHD());
        insertStatusHistory(request.getIdHD(), EntityTrangThaiHoaDon.DA_HUY.ordinal(), null);
        return new ResponseObject<>(null, HttpStatus.CREATED, "Huy hoa don thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> themSanPham(BanHangRequest request) {
        int quantity = parseQuantity(request.getSoLuong());
        Map<String, Object> product = jdbcTemplate.queryForMap("SELECT so_luong, gia_ban FROM ecommerce_catalog.san_pham_chi_tiet WHERE id = ?", request.getIdSP());
        if (intValue(product.get("so_luong")) < quantity) {
            return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
        }
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("SELECT id, so_luong, gia_ban FROM hoa_don_chi_tiet WHERE id_hoa_don = ? AND id_spct = ? ORDER BY created_date DESC", request.getIdHD(), request.getIdSP());
        double currentPrice = doubleValue(product.get("gia_ban"));
        if (existing.isEmpty()) {
            insertDetail(request.getIdHD(), request.getIdSP(), quantity, currentPrice);
            return new ResponseObject<>(null, HttpStatus.OK, "them san pham thanh cong");
        }
        Map<String, Object> detail = existing.get(0);
        if (Math.abs(doubleValue(detail.get("gia_ban")) - currentPrice) > 0.0001D) {
            insertDetail(request.getIdHD(), request.getIdSP(), quantity, currentPrice);
            return new ResponseObject<>(null, HttpStatus.OK, "San pham nay dang dc thay doi gia tu " + detail.get("gia_ban") + "d thanh " + currentPrice);
        }
        int nextQuantity = intValue(detail.get("so_luong")) + quantity;
        if (intValue(product.get("so_luong")) < nextQuantity) {
            return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
        }
        jdbcTemplate.update("UPDATE hoa_don_chi_tiet SET so_luong = ? WHERE id = ?", nextQuantity, detail.get("id"));
        return new ResponseObject<>(null, HttpStatus.OK, "them san pham");
    }

    @Override
    public List<Map<String, Object>> getListGioHang(String id) {
        return jdbcTemplate.queryForList("""
                SELECT ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                       hdct.id AS idHDCT,
                       spct.id AS id,
                       sp.ten_san_pham AS ten,
                       hdct.so_luong AS soLuong,
                       hdct.gia_ban AS giaBan,
                       kc.ten_kich_co AS kichThuoc,
                       ms.mau_sac AS mau,
                       spct.anh_san_pham AS anh
                FROM hoa_don_chi_tiet hdct
                LEFT JOIN ecommerce_catalog.san_pham_chi_tiet spct ON hdct.id_spct = spct.id
                LEFT JOIN ecommerce_catalog.san_pham sp ON spct.id_san_pham = sp.id
                LEFT JOIN ecommerce_catalog.mau_sac ms ON ms.id = spct.id_mau_sac
                LEFT JOIN ecommerce_catalog.kich_co kc ON kc.id = spct.id_kich_co
                WHERE hdct.id_hoa_don = ?
                ORDER BY spct.created_date DESC
                """, id);
    }

    @Override
    public void xoaSanPham(BanHangRequest request) {
        jdbcTemplate.update("DELETE FROM hoa_don_chi_tiet WHERE id = ?", request.getIdHDCT());
    }

    @Override
    public ResponseObject<?> themSoLuong(BanHangRequest request) {
        Map<String, Object> detail = jdbcTemplate.queryForMap("SELECT so_luong, gia_ban FROM hoa_don_chi_tiet WHERE id = ?", request.getIdHDCT());
        Map<String, Object> product = jdbcTemplate.queryForMap("SELECT so_luong, gia_ban FROM ecommerce_catalog.san_pham_chi_tiet WHERE id = ?", request.getIdSP());
        if (Math.abs(doubleValue(detail.get("gia_ban")) - doubleValue(product.get("gia_ban"))) > 0.0001D) {
            return new ResponseObject<>(null, HttpStatus.OK, "San pham nay dang dc thay doi gia tu " + detail.get("gia_ban") + "d thanh " + product.get("gia_ban"));
        }
        int next = intValue(detail.get("so_luong")) + 1;
        if (intValue(product.get("so_luong")) < next) {
            return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
        }
        jdbcTemplate.update("UPDATE hoa_don_chi_tiet SET so_luong = ? WHERE id = ?", next, request.getIdHDCT());
        return new ResponseObject<>(null, HttpStatus.OK, "");
    }

    @Override
    public void xoaSoLuong(BanHangRequest request) {
        Map<String, Object> detail = jdbcTemplate.queryForMap("SELECT so_luong FROM hoa_don_chi_tiet WHERE id = ?", request.getIdHDCT());
        int next = intValue(detail.get("so_luong")) - 1;
        if (next <= 0) {
            jdbcTemplate.update("DELETE FROM hoa_don_chi_tiet WHERE id = ?", request.getIdHDCT());
        } else {
            jdbcTemplate.update("UPDATE hoa_don_chi_tiet SET so_luong = ? WHERE id = ?", next, request.getIdHDCT());
        }
    }

    @Override
    public ResponseObject<?> listKhachHang(BanHangRequest request) {
        String q = like(request.getQ());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id, ten_khach_hang AS ten, so_dien_thoai AS sdt
                FROM ecommerce_user.khach_hang
                WHERE status = 0 AND (? = '' OR ten_khach_hang LIKE ? OR ma_khach_hang LIKE ? OR so_dien_thoai LIKE ?)
                ORDER BY created_date DESC
                LIMIT ? OFFSET ?
                """, q, q, q, q, pageSize(request), offset(request));
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM ecommerce_user.khach_hang
                WHERE status = 0 AND (? = '' OR ten_khach_hang LIKE ? OR ma_khach_hang LIKE ? OR so_dien_thoai LIKE ?)
                """, Long.class, q, q, q, q);
        return new ResponseObject<>(page(rows, request, total), HttpStatus.OK, "lay danh sach khach hang thanh cong");
    }

    @Override
    public void themKhachHang(BanHangRequest request) {
        jdbcTemplate.update("UPDATE hoa_don SET id_khach_hang = ? WHERE id = ?", request.getIdKH(), request.getIdHD());
    }

    @Override
    public ResponseObject<?> themMoiKhachHang(BanHangRequest request) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("INSERT INTO ecommerce_user.khach_hang (id, status, created_date, ma_khach_hang, ten_khach_hang, so_dien_thoai) VALUES (?, 0, ?, ?, ?, ?)",
                id, System.currentTimeMillis(), generateCode("KH"), request.getTen(), request.getSdt());
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", id);
        customer.put("ten", request.getTen());
        customer.put("sdt", request.getSdt());
        return new ResponseObject<>(customer, HttpStatus.OK, "them moi khach hang thanh cong");
    }

    @Override
    public Map<String, Object> getKhachHang(String id) {
        return jdbcTemplate.queryForMap("""
                SELECT kh.id AS id, kh.ten_khach_hang AS ten, kh.so_dien_thoai AS sdt, kh.dia_chi AS diaChi,
                       kh.tinh AS tinh, kh.huyen AS huyen, kh.xa AS xa
                FROM hoa_don hd
                LEFT JOIN ecommerce_user.khach_hang kh ON kh.id = hd.id_khach_hang
                WHERE hd.id = ?
                """, id);
    }

    @Override
    public Map<String, Object> getThanhToan(String id) {
        return jdbcTemplate.queryForMap("SELECT SUM(hdct.gia_ban * hdct.so_luong) AS tongTien FROM hoa_don hd LEFT JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don WHERE hd.id = ?", id);
    }

    @Override
    public List<Map<String, Object>> getPhuongThucThanhToan(String id) {
        return jdbcTemplate.queryForList("SELECT SUM(hdct.gia_ban * hdct.so_luong) AS tongTien, hd.phuong_thuc_thanh_toan AS phuongThucThanhToan FROM hoa_don hd LEFT JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don WHERE hd.id = ? GROUP BY hd.phuong_thuc_thanh_toan", id);
    }

    @Override
    public void capNhatPhuongThucThanhToan(BanHangRequest request) {
        jdbcTemplate.update("UPDATE hoa_don SET phuong_thuc_thanh_toan = ? WHERE id = ?", paymentMethod(request.getPhuongThucThanhToan()), request.getIdHD());
    }

    @Override
    public ResponseObject<?> getAllSanPham(BanHangRequest request) {
        String q = like(request.getQ());
        String status = blankToNull(request.getStatus());
        String idMauSac = blankToNull(request.getIdMauSac());
        String idKichThuoc = blankToNull(request.getIdKichThuoc());
        String idDanhMuc = blankToNull(request.getIdDanhMuc());
        String idChatLieu = blankToNull(request.getIdChatLieu());
        String idThuongHieu = blankToNull(request.getIdThuongHieu());
        String idLoaiDe = blankToNull(request.getIdLoaiDe());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                       spct.id AS id, sp.ten_san_pham AS ten, spct.so_luong AS soLuong,
                       th.ten_thuong_hieu AS tenThuongHieu, ld.ten_loai_de AS tenLoaiDe,
                       cl.ten_chat_lieu AS tenChatLieu, dm.ten_danh_muc AS tenDanhMuc,
                       spct.gia_ban AS giaBan, kc.ten_kich_co AS kichThuoc,
                       ms.mau_sac AS mau, ms.ten_mau_sac AS tenMau,
                       spct.anh_san_pham AS anh, spct.status AS status,
                       (SELECT MAX(spct2.gia_ban) FROM ecommerce_catalog.san_pham_chi_tiet spct2) AS giaMax
                FROM ecommerce_catalog.san_pham_chi_tiet spct
                LEFT JOIN ecommerce_catalog.san_pham sp ON spct.id_san_pham = sp.id
                LEFT JOIN ecommerce_catalog.thuong_hieu th ON th.id = sp.id_thuong_hieu
                LEFT JOIN ecommerce_catalog.kich_co kc ON kc.id = spct.id_kich_co
                LEFT JOIN ecommerce_catalog.loai_de ld ON ld.id = sp.id_loai_de
                LEFT JOIN ecommerce_catalog.danh_muc dm ON dm.id = sp.id_danh_muc
                LEFT JOIN ecommerce_catalog.chat_lieu cl ON cl.id = sp.id_chat_lieu
                LEFT JOIN ecommerce_catalog.mau_sac ms ON ms.id = spct.id_mau_sac
                WHERE spct.so_luong > 0 AND spct.status = 0
                  AND (? = '' OR sp.ten_san_pham LIKE ? OR spct.ma_san_pham LIKE ?)
                  AND (? IS NULL OR spct.status = ?)
                  AND (? IS NULL OR spct.id_mau_sac = ?)
                  AND (? IS NULL OR spct.id_kich_co = ?)
                  AND (? IS NULL OR sp.id_danh_muc = ?)
                  AND (? IS NULL OR sp.id_chat_lieu = ?)
                  AND (? IS NULL OR sp.id_thuong_hieu = ?)
                  AND (? IS NULL OR sp.id_loai_de = ?)
                ORDER BY spct.created_date DESC
                LIMIT ? OFFSET ?
                """, q, q, q, status, status, idMauSac, idMauSac,
                idKichThuoc, idKichThuoc, idDanhMuc, idDanhMuc,
                idChatLieu, idChatLieu, idThuongHieu, idThuongHieu,
                idLoaiDe, idLoaiDe, pageSize(request), offset(request));
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM ecommerce_catalog.san_pham_chi_tiet spct
                LEFT JOIN ecommerce_catalog.san_pham sp ON spct.id_san_pham = sp.id
                WHERE spct.so_luong > 0 AND spct.status = 0
                  AND (? = '' OR sp.ten_san_pham LIKE ? OR spct.ma_san_pham LIKE ?)
                  AND (? IS NULL OR spct.status = ?)
                  AND (? IS NULL OR spct.id_mau_sac = ?)
                  AND (? IS NULL OR spct.id_kich_co = ?)
                  AND (? IS NULL OR sp.id_danh_muc = ?)
                  AND (? IS NULL OR sp.id_chat_lieu = ?)
                  AND (? IS NULL OR sp.id_thuong_hieu = ?)
                  AND (? IS NULL OR sp.id_loai_de = ?)
                """, Long.class, q, q, q, status, status, idMauSac, idMauSac,
                idKichThuoc, idKichThuoc, idDanhMuc, idDanhMuc,
                idChatLieu, idChatLieu, idThuongHieu, idThuongHieu,
                idLoaiDe, idLoaiDe);
        return new ResponseObject<>(page(rows, request, total), HttpStatus.OK, "Lay danh sach san pham chi tiet thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> thanhToanThanhCong(BanHangRequest request) {
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT id_spct, so_luong FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", request.getIdHD());
        for (Map<String, Object> detail : details) {
            Integer stock = jdbcTemplate.queryForObject("SELECT so_luong FROM ecommerce_catalog.san_pham_chi_tiet WHERE id = ?", Integer.class, detail.get("id_spct"));
            if (stock == null || stock < intValue(detail.get("so_luong"))) {
                return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham khong du");
            }
            jdbcTemplate.update("UPDATE ecommerce_catalog.san_pham_chi_tiet SET so_luong = so_luong - ? WHERE id = ?", intValue(detail.get("so_luong")), detail.get("id_spct"));
        }
        int loaiHoaDon = intValue(getOrder(request.getIdHD()).get("loai_hoa_don"));
        int nextStatus = loaiHoaDon == EntityLoaiHoaDon.GIAO_HANG.ordinal()
                ? EntityTrangThaiHoaDon.DA_XAC_NHAN.ordinal()
                : EntityTrangThaiHoaDon.HOAN_THANH.ordinal();
        jdbcTemplate.update("""
                UPDATE hoa_don
                SET trang_thai_hoa_don = ?, tong_tien = ?, ten_hoa_don = ?, dia_chi_giao_hang = ?, so_dien_thoai_khach_hang = ?,
                    phuong_thuc_thanh_toan = ?, phi_van_chuyen = ?, giam_gia = ?, tong_tien_sau_giam = ?, id_voucher = ?
                WHERE id = ?
                """, nextStatus, request.getTienHang(), request.getTen(), request.getDiaChi(), request.getSdt(),
                paymentMethod(request.getPhuongThucThanhToan()), request.getTienShip(), request.getGiamGia(), request.getTongTien(), request.getIdPGG(), request.getIdHD());
        if (request.getIdPGG() != null) {
            jdbcTemplate.update("UPDATE ecommerce_promotion.phieu_giam_gia SET so_luong_phieu = COALESCE(so_luong_phieu, 0) - 1 WHERE id = ?", request.getIdPGG());
        }
        insertStatusHistory(request.getIdHD(), nextStatus, nextStatus == EntityTrangThaiHoaDon.HOAN_THANH.ordinal()
                ? "Don hang da duoc khach hang thanh toan thanh cong."
                : "Don hang da duoc xac nhan va cho giao cho don vi van chuyen.");
        if (nextStatus == EntityTrangThaiHoaDon.HOAN_THANH.ordinal() || "1".equals(request.getPhuongThucThanhToan())) {
            insertPayment(request.getIdHD(), request.getTongTien(), request.getPhuongThucThanhToan(), request.getIdNV());
        }
        return new ResponseObject<>(null, HttpStatus.CREATED, nextStatus == EntityTrangThaiHoaDon.HOAN_THANH.ordinal() ? "Thanh toan thanh cong" : "Xac nhan giao hang thanh cong");
    }

    @Override
    public ResponseObject<?> danhSachPhieuGiamGia(BanHangRequest request) {
        Double total = request.getTienHang();
        if (total == null) {
            total = request.getTongTien();
        }
        if (total == null) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Tong tien khong duoc de trong");
        }
        final double voucherTotal = total;
        List<Map<String, Object>> vouchers = voucherRows(request.getIdKH()).stream()
                .filter(v -> intValue(v.get("so_luong_phieu")) > 0 && doubleValue(v.get("dieu_kien")) <= voucherTotal)
                .peek(v -> v.put("giaTriGiamThucTe", discountValue(v, voucherTotal)))
                .sorted((a, b) -> Double.compare(doubleValue(b.get("giaTriGiamThucTe")), doubleValue(a.get("giaTriGiamThucTe"))))
                .toList();
        return new ResponseObject<>(vouchers, HttpStatus.CREATED, "Lay gia tri phieu giam gia thanh cong");
    }

    @Override
    public ResponseObject<?> giaoHang(String id) {
        int current = intValue(getOrder(id).get("loai_hoa_don"));
        int next = current == EntityLoaiHoaDon.OFFLINE.ordinal() ? EntityLoaiHoaDon.GIAO_HANG.ordinal() : EntityLoaiHoaDon.OFFLINE.ordinal();
        jdbcTemplate.update("UPDATE hoa_don SET loai_hoa_don = ? WHERE id = ?", next, id);
        return new ResponseObject<>(null, HttpStatus.CREATED, "Lay gia tri phieu giam gia thanh cong");
    }

    private List<Map<String, Object>> voucherRows(String customerId) {
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT p.*
                FROM ecommerce_promotion.phieu_giam_gia p
                LEFT JOIN ecommerce_promotion.phieu_giam_gia_chi_tiet_khach_hang pggct ON p.id = pggct.id_phieu_giam_gia
                WHERE p.status = 0
                  AND p.so_luong_phieu > 0
                  AND (p.loai_giam = false OR (p.loai_giam = true AND pggct.id_khach_hang = ?))
                  AND NOT EXISTS (SELECT 1 FROM hoa_don hd WHERE hd.id_voucher = p.id AND hd.id_khach_hang = ?)
                """, customerId, customerId);
    }

    private Map<String, Object> getOrder(String id) {
        return jdbcTemplate.queryForMap("SELECT * FROM hoa_don WHERE id = ?", id);
    }

    private void insertDetail(String orderId, String productDetailId, int quantity, double price) {
        jdbcTemplate.update("""
                INSERT INTO hoa_don_chi_tiet (id, status, created_date, ma_hoa_don_chi_tiet, so_luong, gia_ban, id_spct, id_hoa_don)
                VALUES (?, 0, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID().toString(), System.currentTimeMillis(), generateCode("HDCT"), quantity, price, productDetailId, orderId);
    }

    private void insertStatusHistory(String orderId, int status, String note) {
        jdbcTemplate.update("INSERT INTO lich_su_trang_thai_hoa_don (hoa_don_id, trang_thai, thoi_gian, note) VALUES (?, ?, ?, ?)",
                orderId, status, LocalDateTime.now(), note);
    }

    private void insertPayment(String orderId, Double amount, String type, String staffId) {
        jdbcTemplate.update("INSERT INTO lich_su_thanh_toan (so_tien, thoi_gian, ma_giao_dich, loai_giao_dich, nhan_vien_id, hoa_don_id) VALUES (?, ?, ?, ?, ?, ?)",
                amount, LocalDateTime.now(), UUID.randomUUID().toString(), type, staffId, orderId);
    }

    private static int paymentMethod(String value) {
        if ("0".equals(value)) {
            return EntityPhuongThucThanhToan.TIEN_MAT.ordinal();
        }
        if ("1".equals(value)) {
            return EntityPhuongThucThanhToan.CHUYEN_KHOAN.ordinal();
        }
        return EntityPhuongThucThanhToan.TIEN_MAT_CHUYEN_KHOAN.ordinal();
    }

    private static double discountValue(Map<String, Object> voucher, double total) {
        if (booleanValue(voucher.get("kieu_giam"))) {
            return Math.min(total * doubleValue(voucher.get("phan_tram")) / 100, doubleValue(voucher.get("gia_giam_toi_da")));
        }
        return doubleValue(voucher.get("phan_tram"));
    }

    private static String like(String q) {
        return q == null || q.trim().isEmpty() ? "" : "%" + q.trim() + "%";
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static Map<String, Object> page(List<Map<String, Object>> rows, BanHangRequest request, Long total) {
        long totalElements = total == null ? 0L : total;
        int size = pageSize(request);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", rows);
        result.put("totalPages", size <= 0 ? 0L : (long) Math.ceil((double) totalElements / size));
        result.put("currentPage", Math.max(request.getPage() - 1, 0));
        result.put("totalElements", totalElements);
        return result;
    }

    private static int pageSize(BanHangRequest request) {
        return request.getSize() <= 0 ? 10 : request.getSize();
    }

    private static int offset(BanHangRequest request) {
        return Math.max(request.getPage() - 1, 0) * pageSize(request);
    }

    private static int parseQuantity(String quantity) {
        return quantity == null || quantity.isBlank() ? 1 : Integer.parseInt(quantity);
    }

    private static boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static int intValue(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static double value(Double value) {
        return value == null ? 0D : value;
    }

    private static double doubleValue(Object value) {
        return value == null ? 0D : ((Number) value).doubleValue();
    }

    private static String generateCode(String prefix) {
        return prefix + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
