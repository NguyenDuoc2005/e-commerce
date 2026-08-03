package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.constant.EntityLoaiHoaDon;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.SanPhamChiTietSearchRequest;
import com.ecommerce.order.model.request.ThemSanPhamRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;
import com.ecommerce.order.service.DonMuaService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
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

    public DonMuaServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResponseObject<?> getAllHoaDon(HoaDonSearchRequest request) {
        try {
            String q = like(request.getQ());
            Integer status = request.getStatus() == null ? null : request.getStatus().ordinal();
            List<Map<String, Object>> page = jdbcTemplate.queryForList("""
                    SELECT hd.id AS id,
                           hd.ma_hoa_don AS maHoaDon,
                           spct.anh_san_pham AS anh,
                           sp.ten_san_pham AS tenSanPham,
                           th.ten_thuong_hieu AS tenThuongHieu,
                           ms.ten_mau_sac AS mauSac,
                           kc.ten_kich_co AS kichCo,
                           hdct.so_luong AS soLuong,
                           hdct.gia_ban AS giaBan,
                           hd.trang_thai_hoa_don AS status,
                           hd.tong_tien_sau_giam AS tongTien
                    FROM hoa_don hd
                    JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
                    JOIN san_pham_chi_tiet spct ON spct.id = hdct.id_spct
                    LEFT JOIN san_pham sp ON sp.id = spct.id_san_pham
                    LEFT JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                    LEFT JOIN mau_sac ms ON ms.id = spct.id_mau_sac
                    LEFT JOIN kich_co kc ON kc.id = spct.id_kich_co
                    LEFT JOIN khach_hang kh ON kh.id = hd.id_khach_hang
                    WHERE (? IS NULL OR ? = '' OR LOWER(kh.id) LIKE LOWER(?))
                      AND (? IS NULL OR hd.trang_thai_hoa_don = ?)
                      AND hd.loai_hoa_don = ?
                      AND hd.trang_thai_hoa_don != ?
                    ORDER BY hd.created_date, hd.ma_hoa_don ASC
                    """, q, q, q, status, status, ONLINE, LUU_TAM);
            Long total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(hd.id)
                    FROM hoa_don hd
                    JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
                    LEFT JOIN khach_hang kh ON kh.id = hd.id_khach_hang
                    WHERE (? IS NULL OR ? = '' OR LOWER(kh.id) LIKE LOWER(?))
                      AND (? IS NULL OR hd.trang_thai_hoa_don = ?)
                      AND hd.loai_hoa_don = ?
                      AND hd.trang_thai_hoa_don != ?
                    """, Long.class, q, q, q, status, status, ONLINE, LUU_TAM);
            return new ResponseObject<>(Map.of("page", page, "totalRecords", total == null ? 0 : total, "countByStatus", countOnlineByStatus(q)), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
        } catch (Exception e) {
            return new ResponseObject<>(null, HttpStatus.INTERNAL_SERVER_ERROR, "Loi khi lay danh sach don hang: " + e.getMessage());
        }
    }

    @Override
    public ResponseObject<?> getAllHoaDonByCode(String code) {
        List<Map<String, Object>> page = jdbcTemplate.queryForList("""
                SELECT hd.id AS id,
                       hd.ma_hoa_don AS maHoaDon,
                       spct.anh_san_pham AS anh,
                       sp.ten_san_pham AS tenSanPham,
                       th.ten_thuong_hieu AS tenThuongHieu,
                       ms.ten_mau_sac AS mauSac,
                       kc.ten_kich_co AS kichCo,
                       hdct.so_luong AS soLuong,
                       hdct.gia_ban AS giaBan,
                       hd.trang_thai_hoa_don AS status,
                       hd.tong_tien_sau_giam AS tongTien
                FROM hoa_don hd
                JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
                JOIN san_pham_chi_tiet spct ON spct.id = hdct.id_spct
                LEFT JOIN san_pham sp ON sp.id = spct.id_san_pham
                LEFT JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                LEFT JOIN mau_sac ms ON ms.id = spct.id_mau_sac
                LEFT JOIN kich_co kc ON kc.id = spct.id_kich_co
                WHERE hd.ma_hoa_don = ?
                  AND hd.trang_thai_hoa_don != ?
                ORDER BY hd.created_date, hd.ma_hoa_don ASC
                """, code, LUU_TAM);
        return new ResponseObject<>(Map.of("page", page, "totalRecords", page.size(), "countByStatus", countByCode(code)), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
    }

    @Override
    public ResponseObject<?> getAllSanPhamChiTiet(SanPhamChiTietSearchRequest request) {
        String q = like(request.getQ());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                       spct.id AS id,
                       sp.ten_san_pham AS ten,
                       spct.so_luong AS soLuong,
                       th.ten_thuong_hieu AS tenThuongHieu,
                       ld.ten_loai_de AS tenLoaiDe,
                       cl.ten_chat_lieu AS tenChatLieu,
                       dm.ten_danh_muc AS tenDanhMuc,
                       spct.gia_ban AS giaBan,
                       kc.ten_kich_co AS kichThuoc,
                       ms.mau AS mau,
                       ms.ten_mau_sac AS tenMau,
                       spct.anh_san_pham AS anh,
                       spct.status AS status,
                       (SELECT MAX(spct2.gia_ban) FROM san_pham_chi_tiet spct2) AS giaMax
                FROM san_pham_chi_tiet spct
                LEFT JOIN san_pham sp ON sp.id = spct.id_san_pham
                LEFT JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                LEFT JOIN loai_de ld ON ld.id = sp.id_loai_de
                LEFT JOIN danh_muc dm ON dm.id = sp.id_danh_muc
                LEFT JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu
                LEFT JOIN kich_co kc ON kc.id = spct.id_kich_co
                LEFT JOIN mau_sac ms ON ms.id = spct.id_mau_sac
                WHERE (? IS NULL OR spct.id_san_pham = ?)
                  AND (? IS NULL OR ? = '' OR sp.ten_san_pham LIKE ? OR spct.ma_san_pham LIKE ?)
                  AND (? IS NULL OR spct.status = ?)
                  AND (? IS NULL OR spct.gia_ban >= ?)
                  AND (? IS NULL OR spct.gia_ban <= ?)
                  AND (? IS NULL OR spct.id_kich_co = ?)
                  AND (? IS NULL OR spct.id_mau_sac = ?)
                ORDER BY spct.created_date DESC
                LIMIT ? OFFSET ?
                """, request.getIdSP(), request.getIdSP(), q, q, q, q, request.getEntityStatus(), request.getEntityStatus(),
                request.getPriceMin(), request.getPriceMin(), request.getPriceMax(), request.getPriceMax(), request.getIdKT(), request.getIdKT(),
                request.getIdMS(), request.getIdMS(), pageSize(request), Math.max(request.getPage() - 1, 0) * pageSize(request));
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
        Map<String, Object> sanPham = jdbcTemplate.queryForMap("SELECT id, so_luong, gia_ban FROM san_pham_chi_tiet WHERE id = ?", request.getIdSP());
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("""
                SELECT id, so_luong, gia_ban
                FROM hoa_don_chi_tiet
                WHERE id_hoa_don = ? AND id_spct = ?
                ORDER BY created_date DESC
                """, request.getIdHD(), request.getIdSP());
        int stock = intValue(sanPham.get("so_luong"));
        double currentPrice = doubleValue(sanPham.get("gia_ban"));
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
                LEFT JOIN khach_hang kh ON kh.id = hd.id_khach_hang
                WHERE (? IS NULL OR ? = '' OR LOWER(kh.id) LIKE LOWER(?))
                  AND hd.loai_hoa_don = ?
                  AND hd.trang_thai_hoa_don != ?
                GROUP BY hd.trang_thai_hoa_don
                """, rs -> result.put(EntityTrangThaiHoaDon.values()[rs.getInt("trang_thai_hoa_don")], rs.getLong("total")), q, q, q, ONLINE, LUU_TAM);
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
                """, rs -> result.put(EntityTrangThaiHoaDon.values()[rs.getInt("trang_thai_hoa_don")], rs.getLong("total")), code, LUU_TAM);
        return result;
    }

    private void insertHoaDonChiTiet(String hoaDonId, String sanPhamChiTietId, double price, int quantity) {
        jdbcTemplate.update("""
                INSERT INTO hoa_don_chi_tiet (id, status, created_date, ma_hoa_don_chi_tiet, so_luong, gia_ban, id_spct, id_hoa_don)
                VALUES (?, 0, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID().toString(), System.currentTimeMillis(), generateCodeHoaDonChiTiet(), quantity, price, sanPhamChiTietId, hoaDonId);
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

    private static String generateCodeHoaDonChiTiet() {
        return "HDCT" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
