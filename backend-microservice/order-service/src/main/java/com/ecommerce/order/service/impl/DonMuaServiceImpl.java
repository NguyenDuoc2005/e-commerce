package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.constant.EntityLoaiHoaDon;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.model.request.HoaDonDetailRequest;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.SanPhamChiTietSearchRequest;
import com.ecommerce.order.model.request.ThemSanPhamRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;
import com.ecommerce.order.service.DonMuaService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DonMuaServiceImpl implements DonMuaService {

    private static final int ONLINE = EntityLoaiHoaDon.ONLINE.ordinal();
    private static final int CHO_XAC_NHAN = EntityTrangThaiHoaDon.CHO_XAC_NHAN.ordinal();
    private static final int LUU_TAM = EntityTrangThaiHoaDon.LUU_TAM.ordinal();

    private final JdbcTemplate jdbcTemplate;
    private final CatalogClient catalogClient;

    public DonMuaServiceImpl(JdbcTemplate jdbcTemplate, CatalogClient catalogClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogClient = catalogClient;
    }

    @Override
    public ResponseObject<?> getAllHoaDon(HoaDonSearchRequest request) {
        try {
            String q = like(request.getQ());
            Integer status = request.getStatus() == null ? null : request.getStatus().ordinal();
            List<Map<String, Object>> page = jdbcTemplate.queryForList("""
                    SELECT hd.id AS id,
                           hd.ma_hoa_don AS maHoaDon,
                           hdct.id_spct AS idSPCT,
                           hdct.so_luong AS soLuong,
                           hdct.gia_ban AS giaBan,
                           hd.trang_thai_hoa_don AS status,
                           hd.tong_tien_sau_giam AS tongTien
                    FROM hoa_don hd
                    JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
                    WHERE (? IS NULL OR ? = '' OR LOWER(hd.id_khach_hang) LIKE LOWER(?))
                      AND (? IS NULL OR hd.trang_thai_hoa_don = ?)
                      AND hd.loai_hoa_don = ?
                      AND hd.trang_thai_hoa_don != ?
                    ORDER BY hd.created_date, hd.ma_hoa_don ASC
                    """, q, q, q, status, status, ONLINE, LUU_TAM);
            Long total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(hd.id)
                    FROM hoa_don hd
                    JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
                    WHERE (? IS NULL OR ? = '' OR LOWER(hd.id_khach_hang) LIKE LOWER(?))
                      AND (? IS NULL OR hd.trang_thai_hoa_don = ?)
                      AND hd.loai_hoa_don = ?
                      AND hd.trang_thai_hoa_don != ?
                    """, Long.class, q, q, q, status, status, ONLINE, LUU_TAM);
            return new ResponseObject<>(Map.of("page", enrichOrderRows(page), "totalRecords", total == null ? 0 : total, "countByStatus", countOnlineByStatus(q)), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
        } catch (Exception e) {
            return new ResponseObject<>(null, HttpStatus.INTERNAL_SERVER_ERROR, "Loi khi lay danh sach don hang: " + e.getMessage());
        }
    }

    @Override
    public ResponseObject<?> getAllHoaDonByCode(String code) {
        List<Map<String, Object>> page = jdbcTemplate.queryForList("""
                SELECT hd.id AS id,
                       hd.ma_hoa_don AS maHoaDon,
                       hdct.id_spct AS idSPCT,
                       hdct.so_luong AS soLuong,
                       hdct.gia_ban AS giaBan,
                       hd.trang_thai_hoa_don AS status,
                       hd.tong_tien_sau_giam AS tongTien
                FROM hoa_don hd
                JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
                WHERE hd.ma_hoa_don = ?
                  AND hd.trang_thai_hoa_don != ?
                ORDER BY hd.created_date, hd.ma_hoa_don ASC
                """, code, LUU_TAM);
        return new ResponseObject<>(Map.of("page", enrichOrderRows(page), "totalRecords", page.size(), "countByStatus", countByCode(code)), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
    }

    @Override
    public ResponseObject<?> getHoaDonChiTiet(HoaDonDetailRequest request) {
        if (request.getMaHoaDon() == null || request.getMaHoaDon().isBlank()) {
            return new ResponseObject<>(List.of(), HttpStatus.BAD_REQUEST, "Ma hoa don khong duoc de trong");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT hd.id AS idHoaDon,
                       hd.ma_hoa_don AS maHoaDon,
                       hd.ma_hoa_don AS tenHoaDon,
                       hdct.ma_hoa_don_chi_tiet AS maHoaDonChiTiet,
                       hdct.id_spct AS idSPCT,
                       hdct.so_luong AS soLuong,
                       hdct.gia_ban AS giaBan,
                       (hdct.gia_ban * hdct.so_luong) AS thanhTienSP,
                       (SELECT SUM(hdsub.so_luong * hdsub.gia_ban) FROM hoa_don_chi_tiet hdsub WHERE hdsub.id_hoa_don = hd.id) AS thanhTien,
                       hd.ten_khach_hang AS tenKhachHang,
                       hd.so_dien_thoai_khach_hang AS sdtKH,
                       hd.email AS email,
                       hd.dia_chi_giao_hang AS diaChi,
                       hd.loai_hoa_don AS loaiHoaDon,
                       hd.trang_thai_hoa_don AS trangThaiHoaDon,
                       hd.created_date AS ngayTao,
                       hd.phi_van_chuyen AS phiVanChuyen,
                       hd.id_voucher AS maVoucher,
                       hd.id_voucher AS tenVoucher,
                       hd.giam_gia AS giaTriVoucher,
                       hd.tong_tien_sau_giam AS tongTienSauGiam,
                       hd.tong_tien_sau_giam AS tongTien,
                       hd.phuong_thuc_thanh_toan AS phuongThucThanhToan,
                       hd.du_no AS duNo,
                       hd.hoan_phi AS hoanPhi
                FROM hoa_don_chi_tiet hdct
                JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
                WHERE hd.ma_hoa_don = ?
                  AND hd.loai_hoa_don = ?
                  AND hd.trang_thai_hoa_don != ?
                ORDER BY hdct.created_date ASC
                """, request.getMaHoaDon(), ONLINE, LUU_TAM);
        return new ResponseObject<>(enrichOrderDetailRows(rows), HttpStatus.OK, "Lay danh sach chi tiet hoa don thanh cong");
    }

    @Override
    public List<Map<String, Object>> getCustomerOrderHistory(String customerId) {
        return jdbcTemplate.queryForList("""
                SELECT hd.id AS id,
                       hd.ma_hoa_don AS ma,
                       hd.ten_hoa_don AS ten,
                       hd.so_dien_thoai_khach_hang AS sdt,
                       hd.ten_khach_hang AS tenKH,
                       hd.phi_van_chuyen AS phiVanChuyen,
                       hd.dia_chi_giao_hang AS diaChi,
                       hd.tong_tien_sau_giam AS tongTienSauGiam,
                       hd.tong_tien AS tongTien,
                       hd.ghi_chu AS ghiChu,
                       hd.phuong_thuc_thanh_toan AS phuongThucThanhToan,
                       hd.loai_hoa_don AS loaiHoaDon,
                       hd.trang_thai_hoa_don AS trangThaiHoaDon,
                       hd.created_date AS ngayTao
                FROM hoa_don hd
                WHERE hd.id_khach_hang = ?
                  AND hd.loai_hoa_don = ?
                ORDER BY hd.created_date DESC
                """, customerId, ONLINE);
    }

    @Override
    public ResponseObject<?> getAllSanPhamChiTiet(SanPhamChiTietSearchRequest request) {
        List<Map<String, Object>> rows = catalogClient.searchProductDetails(like(request.getQ()), stringValue(request.getEntityStatus()),
                request.getIdMS(), request.getIdKT(), null, null, null, null, request.getIdSP(), request.getPriceMin(), request.getPriceMax());
        int offset = Math.max(request.getPage() - 1, 0) * pageSize(request);
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).put("stt", i + 1);
        }
        rows = slice(rows, offset, pageSize(request));
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay danh sach san pham chi tiet thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> suaThongTin(UpdateDeliveryRequest request) {
        Map<String, Object> hoaDon = jdbcTemplate.queryForMap("SELECT id, trang_thai_hoa_don, tong_tien_sau_giam, du_no, hoan_phi FROM hoa_don WHERE ma_hoa_don = ?", request.getMaHoaDon());
        if (((Number) hoaDon.get("trang_thai_hoa_don")).intValue() != CHO_XAC_NHAN) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Chi co the cap nhat thong tin giao hang khi don hang dang cho xac nhan");
        }
        double oldTotal = doubleValue(hoaDon.get("tong_tien_sau_giam"));
        double newTotal = doubleValue(request.getTongTienSauGiam());
        double duNo = doubleValue(hoaDon.get("du_no"));
        double hoanPhi = doubleValue(hoaDon.get("hoan_phi"));
        if (oldTotal > newTotal) {
            if (hoaDon.get("du_no") != null) {
                duNo = duNo + oldTotal - newTotal;
                if (hoaDon.get("hoan_phi") != null && hoanPhi > 0) {
                    if (hoanPhi - (newTotal - oldTotal) <= 0) {
                        duNo = 0D;
                    } else {
                        duNo = duNo - (oldTotal - newTotal);
                        hoanPhi = 0D;
                    }
                }
            } else {
                hoanPhi = newTotal - oldTotal;
            }
        } else if (oldTotal < newTotal) {
            if (hoaDon.get("hoan_phi") != null) {
                hoanPhi = hoanPhi + newTotal - oldTotal;
                if (hoaDon.get("du_no") != null && duNo > 0) {
                    if (duNo - (oldTotal - newTotal) <= 0) {
                        duNo = 0D;
                    } else {
                        duNo = 0D;
                    }
                }
            } else {
                duNo = newTotal - oldTotal;
            }
        }
        jdbcTemplate.update("""
                UPDATE hoa_don
                SET ten_khach_hang = ?, so_dien_thoai_khach_hang = ?, email = ?, dia_chi_giao_hang = ?,
                    phi_van_chuyen = ?, tong_tien_sau_giam = ?, du_no = ?, hoan_phi = ?
                WHERE ma_hoa_don = ?
                """, request.getTenKhachHang(), request.getSdtKhachHang(), request.getEmail(), request.getDiaChi(),
                request.getPhiVanChuyen(), newTotal, duNo, hoanPhi, request.getMaHoaDon());
        return new ResponseObject<>(Map.of("maHoaDon", request.getMaHoaDon()), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> themSanPham(ThemSanPhamRequest request) {
        Map<String, Object> sanPham = catalogClient.getProductDetail(request.getIdSP());
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("""
                SELECT id, so_luong, gia_ban
                FROM hoa_don_chi_tiet
                WHERE id_hoa_don = ? AND id_spct = ?
                ORDER BY created_date DESC
                """, request.getIdHD(), request.getIdSP());
        int stock = intValue(sanPham.get("soLuong"));
        double currentPrice = doubleValue(sanPham.get("giaBan"));
        if (existing.isEmpty()) {
            if (stock < 1) {
                return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
            }
            insertHoaDonChiTiet(request.getIdHD(), request.getIdSP(), currentPrice, 1);
            return new ResponseObject<>(null, HttpStatus.OK, "them san pham thanh cong");
        }

        Map<String, Object> detail = existing.get(0);
        double oldPrice = doubleValue(detail.get("gia_ban"));
        if (Math.abs(oldPrice - currentPrice) > 0.0001D) {
            if (stock < 1) {
                return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
            }
            insertHoaDonChiTiet(request.getIdHD(), request.getIdSP(), currentPrice, 1);
            return new ResponseObject<>(null, HttpStatus.OK, "San pham nay dang duoc thay doi gia tu " + oldPrice + "d thanh " + currentPrice);
        }
        int nextQuantity = intValue(detail.get("so_luong")) + 1;
        if (stock < nextQuantity) {
            return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
        }
        jdbcTemplate.update("UPDATE hoa_don_chi_tiet SET so_luong = ? WHERE id = ?", nextQuantity, detail.get("id"));
        return new ResponseObject<>(null, HttpStatus.OK, "them san pham");
    }

    private Map<EntityTrangThaiHoaDon, Long> countOnlineByStatus(String q) {
        Map<EntityTrangThaiHoaDon, Long> result = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT hd.trang_thai_hoa_don, COUNT(hd.id) AS total
                FROM hoa_don hd
                WHERE (? IS NULL OR ? = '' OR LOWER(hd.id_khach_hang) LIKE LOWER(?))
                  AND hd.loai_hoa_don = ?
                  AND hd.trang_thai_hoa_don != ?
                GROUP BY hd.trang_thai_hoa_don
                """, (RowCallbackHandler) rs ->
                result.put(EntityTrangThaiHoaDon.values()[rs.getInt("trang_thai_hoa_don")], rs.getLong("total")), q, q, q, ONLINE, LUU_TAM);
        return result;
    }

    private Map<EntityTrangThaiHoaDon, Long> countByCode(String code) {
        Map<EntityTrangThaiHoaDon, Long> result = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT hd.trang_thai_hoa_don, COUNT(hd.id) AS total
                FROM hoa_don hd
                WHERE hd.ma_hoa_don = ?
                  AND hd.trang_thai_hoa_don != ?
                GROUP BY hd.trang_thai_hoa_don
                """, (RowCallbackHandler) rs ->
                result.put(EntityTrangThaiHoaDon.values()[rs.getInt("trang_thai_hoa_don")], rs.getLong("total")), code, LUU_TAM);
        return result;
    }

    private void insertHoaDonChiTiet(String hoaDonId, String sanPhamChiTietId, double price, int quantity) {
        jdbcTemplate.update("""
                INSERT INTO hoa_don_chi_tiet (id, status, created_date, ma_hoa_don_chi_tiet, so_luong, gia_ban, id_spct, id_hoa_don)
                VALUES (?, 0, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID().toString(), System.currentTimeMillis(), generateCodeHoaDonChiTiet(), quantity, price, sanPhamChiTietId, hoaDonId);
    }

    private List<Map<String, Object>> enrichOrderRows(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> enriched = new LinkedHashMap<>(row);
            Object productDetailId = row.get("idSPCT");
            if (productDetailId == null) {
                productDetailId = row.get("idspct");
            }
            if (productDetailId != null) {
                Map<String, Object> product = catalogClient.getProductDetail(String.valueOf(productDetailId));
                enriched.put("anh", product.get("anh"));
                enriched.put("tenSanPham", product.get("ten"));
                enriched.put("tenThuongHieu", product.get("tenThuongHieu"));
                enriched.put("mauSac", product.get("tenMau"));
                enriched.put("kichCo", product.get("kichThuoc"));
            }
            return enriched;
        }).toList();
    }

    private List<Map<String, Object>> enrichOrderDetailRows(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> enriched = new LinkedHashMap<>(row);
            Object productDetailId = row.get("idSPCT");
            if (productDetailId == null) {
                productDetailId = row.get("idspct");
            }
            Map<String, Object> product = productDetailId == null ? Map.of() : safeProductDetail(String.valueOf(productDetailId));
            enriched.put("tenSanPham", firstNonNull(product.get("tenSanPham"), product.get("ten"), productDetailId));
            enriched.put("anhSanPham", firstNonNull(product.get("anh"), product.get("hinhAnh")));
            enriched.put("thuongHieu", firstNonNull(product.get("tenThuongHieu"), product.get("thuongHieu")));
            enriched.put("mauSac", firstNonNull(product.get("tenMauSac"), product.get("tenMau"), product.get("mau")));
            enriched.put("size", firstNonNull(product.get("tenKichCo"), product.get("kichThuoc"), product.get("size")));
            enriched.put("xuatSu", firstNonNull(product.get("tenXuatXu"), product.get("xuatXu")));
            return enriched;
        }).toList();
    }

    private Map<String, Object> safeProductDetail(String productDetailId) {
        try {
            Map<String, Object> product = catalogClient.getProductDetail(productDetailId);
            return product == null ? Map.of() : product;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static List<Map<String, Object>> slice(List<Map<String, Object>> rows, int offset, int size) {
        if (offset >= rows.size()) {
            return List.of();
        }
        return rows.subList(offset, Math.min(rows.size(), offset + size));
    }

    private static String like(String q) {
        return q == null || q.trim().isEmpty() ? "" : "%" + q.trim() + "%";
    }

    private static int intValue(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value == null ? 0D : Double.parseDouble(String.valueOf(value));
    }

    private static int pageSize(SanPhamChiTietSearchRequest request) {
        return request.getSize() <= 0 ? 10 : request.getSize();
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Enum<?> enumValue) {
            return String.valueOf(enumValue.ordinal());
        }
        return String.valueOf(value);
    }

    private static String generateCodeHoaDonChiTiet() {
        return "HDCT" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
