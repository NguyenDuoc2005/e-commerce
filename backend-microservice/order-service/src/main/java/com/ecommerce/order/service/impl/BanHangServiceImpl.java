package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.constant.EntityLoaiHoaDon;
import com.ecommerce.order.constant.EntityPhuongThucThanhToan;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PromotionClient;
import com.ecommerce.order.client.UserClient;
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
    private final CatalogClient catalogClient;
    private final PromotionClient promotionClient;
    private final UserClient userClient;

    public BanHangServiceImpl(JdbcTemplate jdbcTemplate, CatalogClient catalogClient, PromotionClient promotionClient, UserClient userClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogClient = catalogClient;
        this.promotionClient = promotionClient;
        this.userClient = userClient;
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
        Map<String, Object> product = catalogClient.getProductDetail(request.getIdSP());
        if (intValue(product.get("soLuong")) < quantity) {
            return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
        }
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("SELECT id, so_luong, gia_ban FROM hoa_don_chi_tiet WHERE id_hoa_don = ? AND id_spct = ? ORDER BY created_date DESC", request.getIdHD(), request.getIdSP());
        double currentPrice = doubleValue(product.get("giaBan"));
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
        if (intValue(product.get("soLuong")) < nextQuantity) {
            return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
        }
        jdbcTemplate.update("UPDATE hoa_don_chi_tiet SET so_luong = ? WHERE id = ?", nextQuantity, detail.get("id"));
        return new ResponseObject<>(null, HttpStatus.OK, "them san pham");
    }

    @Override
    public List<Map<String, Object>> getListGioHang(String id) {
        List<Map<String, Object>> details = jdbcTemplate.queryForList("""
                SELECT id AS idHDCT, id_spct AS id, so_luong AS soLuong, gia_ban AS giaBan
                FROM hoa_don_chi_tiet
                WHERE id_hoa_don = ?
                ORDER BY created_date DESC
                """, id);
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> row = new LinkedHashMap<>(details.get(i));
            Map<String, Object> product = catalogClient.getProductDetail(String.valueOf(row.get("id")));
            row.put("stt", i + 1);
            row.put("ten", product.get("ten"));
            row.put("kichThuoc", product.get("kichThuoc"));
            row.put("mau", product.get("mau"));
            row.put("anh", product.get("anh"));
            details.set(i, row);
        }
        return details;
    }

    @Override
    public void xoaSanPham(BanHangRequest request) {
        jdbcTemplate.update("DELETE FROM hoa_don_chi_tiet WHERE id = ?", request.getIdHDCT());
    }

    @Override
    public ResponseObject<?> themSoLuong(BanHangRequest request) {
        Map<String, Object> detail = jdbcTemplate.queryForMap("SELECT so_luong, gia_ban FROM hoa_don_chi_tiet WHERE id = ?", request.getIdHDCT());
        Map<String, Object> product = catalogClient.getProductDetail(request.getIdSP());
        if (Math.abs(doubleValue(detail.get("gia_ban")) - doubleValue(product.get("giaBan"))) > 0.0001D) {
            return new ResponseObject<>(null, HttpStatus.OK, "San pham nay dang dc thay doi gia tu " + detail.get("gia_ban") + "d thanh " + product.get("giaBan"));
        }
        int next = intValue(detail.get("so_luong")) + 1;
        if (intValue(product.get("soLuong")) < next) {
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
        List<Map<String, Object>> all = userClient.searchCustomers(request.getQ());
        return new ResponseObject<>(page(slice(all, offset(request), pageSize(request)), request, (long) all.size()), HttpStatus.OK, "lay danh sach khach hang thanh cong");
    }

    @Override
    public void themKhachHang(BanHangRequest request) {
        jdbcTemplate.update("UPDATE hoa_don SET id_khach_hang = ? WHERE id = ?", request.getIdKH(), request.getIdHD());
    }

    @Override
    public ResponseObject<?> themMoiKhachHang(BanHangRequest request) {
        Map<String, Object> customer = userClient.createCustomer(request.getTen(), request.getSdt());
        return new ResponseObject<>(customer, HttpStatus.OK, "them moi khach hang thanh cong");
    }

    @Override
    public Map<String, Object> getKhachHang(String id) {
        Map<String, Object> order = getOrder(id);
        Object customerId = order.get("id_khach_hang");
        return customerId == null ? Map.of() : userClient.getCustomer(String.valueOf(customerId));
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
        List<Map<String, Object>> rows = catalogClient.searchProductDetails(q, status, idMauSac, idKichThuoc, idDanhMuc, idChatLieu, idThuongHieu, idLoaiDe, null, null, null);
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).put("stt", i + 1);
        }
        return new ResponseObject<>(page(slice(rows, offset(request), pageSize(request)), request, (long) rows.size()), HttpStatus.OK, "Lay danh sach san pham chi tiet thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> thanhToanThanhCong(BanHangRequest request) {
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT id_spct, so_luong FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", request.getIdHD());
        for (Map<String, Object> detail : details) {
            Map<String, Object> product = catalogClient.getProductDetail(String.valueOf(detail.get("id_spct")));
            if (intValue(product.get("soLuong")) < intValue(detail.get("so_luong"))) {
                return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham khong du");
            }
            catalogClient.adjustStock(String.valueOf(detail.get("id_spct")), -intValue(detail.get("so_luong")));
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
            promotionClient.decrementVoucher(request.getIdPGG());
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
        return promotionClient.getApplicableVouchers(customerId).stream()
                .filter(v -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM hoa_don WHERE id_voucher = ? AND id_khach_hang = ?",
                        Integer.class, v.get("id"), customerId) == 0)
                .toList();
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

    private static List<Map<String, Object>> slice(List<Map<String, Object>> rows, int offset, int size) {
        if (offset >= rows.size()) {
            return List.of();
        }
        return rows.subList(offset, Math.min(rows.size(), offset + size));
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
