package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PromotionClient;
import com.ecommerce.order.client.UserClient;
import com.ecommerce.order.constant.EntityLoaiHoaDon;
import com.ecommerce.order.constant.EntityPhuongThucThanhToan;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import com.ecommerce.order.model.request.CheckoutProductItem;
import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.request.VoucherPaymentRequest;
import com.ecommerce.order.service.CheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final int ACTIVE = 0;
    private static final int ONLINE = EntityLoaiHoaDon.ONLINE.ordinal();
    private static final int CHO_XAC_NHAN = EntityTrangThaiHoaDon.CHO_XAC_NHAN.ordinal();
    private static final int LUU_TAM = EntityTrangThaiHoaDon.LUU_TAM.ordinal();

    private final JdbcTemplate jdbcTemplate;
    private final CatalogClient catalogClient;
    private final PromotionClient promotionClient;
    private final CartClient cartClient;
    private final UserClient userClient;

    @Value("${vnpay.tmn-code:}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:}")
    private String vnpHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url:http://localhost:8386/api/orders/vnpay-return}")
    private String vnpReturnUrl;

    public CheckoutServiceImpl(JdbcTemplate jdbcTemplate, CatalogClient catalogClient, PromotionClient promotionClient, CartClient cartClient, UserClient userClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogClient = catalogClient;
        this.promotionClient = promotionClient;
        this.cartClient = cartClient;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public Object createOrder(CheckoutRequest request) {
        if (!hasStock(request)) {
            clearCartItemsWhenOutOfStock(request);
            return null;
        }
        String orderId = insertOrder(request, CHO_XAC_NHAN);
        insertStatusHistory(orderId, CHO_XAC_NHAN, "Don hang da duoc dat va cho duoc xac nhan.");
        if (!"TIEN_MAT".equals(request.getHinhThucThanhToan())) {
            insertPayment(orderId, request.getTongCong(), "VNPAY".equals(request.getHinhThucThanhToan()) ? "CHUYEN_KHOAN" : "TIEN_MAT", null);
        }
        applyVoucher(request.getMaGiamGia());
        insertDetailsAndCommitInventory(orderId, request);
        clearCartItems(request);
        return getOrder(orderId);
    }

    @Override
    @Transactional
    public Map<String, String> createVNPayPaymentUrl(CheckoutRequest request, String ipAddr) {
        validateVNPayConfig();
        if (!hasStock(request)) {
            clearCartItemsWhenOutOfStock(request);
            return null;
        }
        String orderId = insertOrder(request, LUU_TAM);
        insertPayment(orderId, request.getTongCong(), "VNPAY".equals(request.getHinhThucThanhToan()) ? "CHUYEN_KHOAN" : "TIEN_MAT", null);
        insertDetails(orderId, request);

        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String expireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_Amount", String.valueOf((long) (value(request.getTongCong()) * 100)));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);
        params.put("vnp_SecureHash", hmacSHA512(vnpHashSecret, toHashData(params)));

        StringBuilder paymentUrl = new StringBuilder(vnpPayUrl).append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paymentUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append("&");
        }
        paymentUrl.deleteCharAt(paymentUrl.length() - 1);
        return Map.of("orderId", orderId, "paymentUrl", paymentUrl.toString());
    }

    @Override
    @Transactional
    public boolean handleVNPayReturn(Map<String, String> params) {
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        if (!hmacSHA512(vnpHashSecret, toHashData(new TreeMap<>(params))).equals(secureHash)) {
            return false;
        }
        if (!"00".equals(params.get("vnp_ResponseCode"))) {
            return false;
        }
        String orderId = params.get("vnp_TxnRef");
        jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hoa_don = ? WHERE id = ?", CHO_XAC_NHAN, orderId);
        insertStatusHistory(orderId, CHO_XAC_NHAN, "Don hang da duoc dat va cho duoc xac nhan.");
        Map<String, Object> order = getOrder(orderId);
        Object voucherId = order.get("id_voucher");
        if (voucherId != null) {
            promotionClient.decrementVoucher(String.valueOf(voucherId));
        }
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT id_spct, so_luong FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", orderId);
        for (Map<String, Object> detail : details) {
            catalogClient.adjustStock(String.valueOf(detail.get("id_spct")), -intValue(detail.get("so_luong")));
        }
        clearCartItemsByOrder(orderId);
        return true;
    }

    @Override
    public ResponseObject<?> getPhieuGiamGia(VoucherPaymentRequest request) {
        Map<String, Object> voucher = promotionClient.getVoucherByCode(request.getMaPGG());
        if (voucher == null || voucher.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia khong ton tai");
        }
        if (intValue(voucher.get("status")) == 1) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia nay da het han su dung");
        }
        if (intValue(voucher.get("so_luong_phieu")) <= 0) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia nay da het");
        }
        if (booleanValue(voucher.get("loai_giam")) && !isVoucherAssigned(String.valueOf(voucher.get("id")), request.getIdKH())) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia khong ap dung cho tai khoan nay");
        }
        if (doubleValue(voucher.get("dieu_kien")) > value(request.getTongTien())) {
            double remain = doubleValue(voucher.get("dieu_kien")) - value(request.getTongTien());
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Don hang chua du de ap dung phieu giam gia hay mua them " + remain + "d de ap phieu giam gia");
        }
        return new ResponseObject<>(voucherResponse(voucher, value(request.getTongTien())), HttpStatus.OK, "Ap dung phieu giam gia thanh cong");
    }

    @Override
    public ResponseObject<?> getAllApplicablePGG(String idKhachHang, Double tongTien) {
        List<Map<String, Object>> rows = new java.util.ArrayList<>(promotionClient.getApplicableVouchers(idKhachHang));
        rows.removeIf(row -> voucherUsedByCustomer(String.valueOf(row.get("id")), idKhachHang));
        rows.removeIf(row -> intValue(row.get("so_luong_phieu")) <= 0 || doubleValue(row.get("dieu_kien")) > value(tongTien));
        rows = rows.stream()
                .map(row -> voucherResponse(row, value(tongTien)))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        rows.sort((a, b) -> Double.compare(doubleValue(b.get("giaTriGiamThucTe")), doubleValue(a.get("giaTriGiamThucTe"))));
        return new ResponseObject<>(rows, HttpStatus.OK, "Danh sach phieu giam gia hop le");
    }

    private Map<String, Object> voucherResponse(Map<String, Object> voucher, double total) {
        Map<String, Object> row = new java.util.LinkedHashMap<>(voucher);
        row.put("ma", voucher.get("ma_phieu_giam_gia"));
        row.put("ten", voucher.get("ten_phieu_giam_gia"));
        row.put("phanTramGiam", voucher.get("phan_tram"));
        row.put("giaGiam", voucher.get("gia_giam_toi_da"));
        row.put("kieuGiam", voucher.get("kieu_giam"));
        row.put("loaiGiam", voucher.get("loai_giam"));
        row.put("giaTriGiamThucTe", discountValue(voucher, total));
        return row;
    }

    @Override
    public ResponseObject<?> getKhachHang(String id) {
        Map<String, Object> customer = userClient.getCustomer(id);
        return customer == null || customer.isEmpty()
                ? new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khach hang khong ton tai")
                : new ResponseObject<>(customer, HttpStatus.OK, "Lay khach hang thanh cong");
    }

    private String insertOrder(CheckoutRequest request, int status) {
        String id = UUID.randomUUID().toString();
        String voucherId = voucherId(request.getMaGiamGia());
        String customerId = request.getKhachHang() == null || request.getKhachHang().isBlank() || "khach le".equalsIgnoreCase(request.getKhachHang()) || "khÃ¡ch láº»".equals(request.getKhachHang())
                ? null
                : request.getKhachHang();
        int paymentMethod = "TIEN_MAT".equals(request.getHinhThucThanhToan())
                ? EntityPhuongThucThanhToan.TIEN_MAT.ordinal()
                : EntityPhuongThucThanhToan.CHUYEN_KHOAN.ordinal();
        jdbcTemplate.update("""
                INSERT INTO hoa_don (id, status, created_date, ma_hoa_don, so_dien_thoai_khach_hang, ten_khach_hang,
                    dia_chi_giao_hang, giam_gia, loai_hoa_don, tong_tien, tong_tien_sau_giam, phuong_thuc_thanh_toan,
                    email, ghi_chu, phi_van_chuyen, trang_thai_hoa_don, id_voucher, id_khach_hang)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, ACTIVE, System.currentTimeMillis(), generateCode("HD"), request.getSoDienThoai(), request.getHoTen(),
                request.getDiaChi(), request.getGiamGia(), ONLINE, request.getTongTien(), request.getTongCong(), paymentMethod,
                request.getEmail(), request.getGhiChu(), request.getPhiShip(), status, voucherId, customerId);
        return id;
    }

    private void insertDetailsAndCommitInventory(String orderId, CheckoutRequest request) {
        insertDetails(orderId, request);
        if (request.getSanPham() != null) {
            for (CheckoutProductItem item : request.getSanPham()) {
                catalogClient.adjustStock(item.getId(), -value(item.getQuantity()));
            }
        }
    }

    private void insertDetails(String orderId, CheckoutRequest request) {
        if (request.getSanPham() == null) {
            return;
        }
        for (CheckoutProductItem item : request.getSanPham()) {
            Double price = doubleValue(catalogClient.getProductDetail(item.getId()).get("giaBan"));
            jdbcTemplate.update("""
                    INSERT INTO hoa_don_chi_tiet (id, status, created_date, ma_hoa_don_chi_tiet, so_luong, gia_ban, id_spct, id_hoa_don)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, UUID.randomUUID().toString(), ACTIVE, System.currentTimeMillis(), generateCode("HDCT"), item.getQuantity(), price, item.getId(), orderId);
        }
    }

    private boolean hasStock(CheckoutRequest request) {
        if (request.getSanPham() == null) {
            return true;
        }
        for (CheckoutProductItem item : request.getSanPham()) {
            Integer stock = intValue(catalogClient.getProductDetail(item.getId()).get("soLuong"));
            if (stock == null || stock < value(item.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    private void clearCartItemsWhenOutOfStock(CheckoutRequest request) {
        if (request.getSanPham() == null) {
            return;
        }
        clearCartItems(request);
    }

    private void clearCartItems(CheckoutRequest request) {
        if (request.getKhachHang() == null || request.getSanPham() == null || isRetailCustomer(request.getKhachHang())) {
            return;
        }
        cartClient.deleteItems(request.getKhachHang(), request.getSanPham().stream().map(CheckoutProductItem::getId).toList());
    }

    private void clearCartItemsByOrder(String orderId) {
        Map<String, Object> order = getOrder(orderId);
        Object customerId = order.get("id_khach_hang");
        if (customerId == null) {
            return;
        }
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT id_spct FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", orderId);
        cartClient.deleteItems(String.valueOf(customerId), details.stream().map(detail -> String.valueOf(detail.get("id_spct"))).toList());
    }

    private void applyVoucher(String code) {
        String voucherId = voucherId(code);
        if (voucherId != null) {
            promotionClient.decrementVoucher(voucherId);
        }
    }

    private String voucherId(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Map<String, Object> voucher = promotionClient.getVoucherByCode(code);
        return voucher == null || voucher.isEmpty() ? null : String.valueOf(voucher.get("id"));
    }

    private boolean isVoucherAssigned(String voucherId, String customerId) {
        return promotionClient.isVoucherAssigned(voucherId, customerId);
    }

    private boolean voucherUsedByCustomer(String voucherId, String customerId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM hoa_don
                WHERE id_voucher = ? AND id_khach_hang = ? AND trang_thai_hoa_don = ?
                """, Integer.class, voucherId, customerId, EntityTrangThaiHoaDon.HOAN_THANH.ordinal());
        return count != null && count > 0;
    }

    private Map<String, Object> getOrder(String orderId) {
        return jdbcTemplate.queryForMap("SELECT * FROM hoa_don WHERE id = ?", orderId);
    }

    private void insertStatusHistory(String orderId, int status, String note) {
        jdbcTemplate.update("""
                INSERT INTO lich_su_trang_thai_hoa_don (hoa_don_id, trang_thai, thoi_gian, note)
                VALUES (?, ?, ?, ?)
                """, orderId, status, LocalDateTime.now(), note);
    }

    private void insertPayment(String orderId, Double amount, String type, String note) {
        jdbcTemplate.update("""
                INSERT INTO lich_su_thanh_toan (so_tien, thoi_gian, ma_giao_dich, loai_giao_dich, hoa_don_id, ghi_chu)
                VALUES (?, ?, ?, ?, ?, ?)
                """, amount, LocalDateTime.now(), UUID.randomUUID().toString(), type, orderId, note);
    }

    private static String toHashData(Map<String, String> params) {
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            hashData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        if (!hashData.isEmpty()) {
            hashData.deleteCharAt(hashData.length() - 1);
        }
        return hashData.toString();
    }

    private void validateVNPayConfig() {
        if (vnpTmnCode == null || vnpTmnCode.isBlank() || vnpHashSecret == null || vnpHashSecret.isBlank()) {
            throw new IllegalStateException("VNPay config is missing vnpay.tmn-code or vnpay.hash-secret");
        }
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign VNPay data", e);
        }
    }

    private static double discountValue(Map<String, Object> voucher, double total) {
        if (!booleanValue(voucher.get("kieu_giam"))) {
            return doubleValue(voucher.get("gia_giam_toi_da"));
        }
        return Math.min(total * doubleValue(voucher.get("phan_tram")) / 100, doubleValue(voucher.get("gia_giam_toi_da")));
    }

    private static boolean booleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static boolean isRetailCustomer(String customer) {
        return "khach le".equalsIgnoreCase(customer) || "khÃ¡ch láº»".equals(customer);
    }

    private static int intValue(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static int value(Integer value) {
        return value == null ? 0 : value;
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
