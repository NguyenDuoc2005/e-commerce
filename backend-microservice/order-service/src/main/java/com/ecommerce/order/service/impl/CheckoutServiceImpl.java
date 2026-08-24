package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PromotionClient;
import com.ecommerce.order.client.UserClient;
import com.ecommerce.order.constant.OrderTypeConstant;
import com.ecommerce.order.constant.EntityPhuongThucThanhToan;
import com.ecommerce.order.constant.OrderStatusConstant;
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
    private static final int ONLINE = OrderTypeConstant.ONLINE.ordinal();
    private static final int CHO_XAC_NHAN = OrderStatusConstant.CHO_XAC_NHAN.ordinal();
    private static final int LUU_TAM = OrderStatusConstant.LUU_TAM.ordinal();

    private final JdbcTemplate jdbcTemplate;
    private final CatalogClient catalogClient;
    private final PromotionClient promotionClient;
    private final CartClient cartClient;
    private final UserClient userClient;
    private final com.ecommerce.order.client.SellerClient sellerClient;

    @Value("${vnpay.tmn-code:}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:}")
    private String vnpHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url:http://localhost:8386/api/orders/vnpay-return}")
    private String vnpReturnUrl;

    public CheckoutServiceImpl(JdbcTemplate jdbcTemplate, CatalogClient catalogClient, PromotionClient promotionClient, CartClient cartClient, UserClient userClient, com.ecommerce.order.client.SellerClient sellerClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogClient = catalogClient;
        this.promotionClient = promotionClient;
        this.cartClient = cartClient;
        this.userClient = userClient;
        this.sellerClient = sellerClient;
    }

    @Override
    @Transactional
    public Object createOrder(CheckoutRequest request) {
        if (!hasStock(request)) {
            clearCartItemsWhenOutOfStock(request);
            return null;
        }
        String orderId = insertOrder(request, CHO_XAC_NHAN);
        insertStatusHistory(orderId, CHO_XAC_NHAN, "Don hang da duoc dat va cho duoc wardc nhan.");
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
        insertDetails(orderId, request, LUU_TAM);

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
        jdbcTemplate.update("UPDATE orders SET order_status = ? WHERE id = ?", CHO_XAC_NHAN, orderId);
        jdbcTemplate.update("UPDATE order_seller SET order_status = ? WHERE order_id = ?", CHO_XAC_NHAN, orderId);
        insertStatusHistory(orderId, CHO_XAC_NHAN, "Don hang da duoc dat va cho duoc wardc nhan.");
        Map<String, Object> order = getOrder(orderId);
        Object voucherId = order.get("voucher_id");
        if (voucherId != null) {
            promotionClient.decrementVoucher(String.valueOf(voucherId));
        }
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT product_variant_id, quantity FROM order_item WHERE order_id = ?", orderId);
        for (Map<String, Object> detail : details) {
            catalogClient.adjustStock(String.valueOf(detail.get("product_variant_id")), -intValue(detail.get("quantity")));
        }
        clearCartItemsByOrder(orderId);
        return true;
    }

    @Override
    public ResponseObject<?> getVoucher(VoucherPaymentRequest request) {
        Map<String, Object> voucher = promotionClient.getVoucherByCode(request.getMaPGG());
        if (voucher == null || voucher.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia khong ton tai");
        }
        if (intValue(voucher.get("status")) == 1) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia nay da het han su dung");
        }
        if (intValue(voucher.get("quantity")) <= 0) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia nay da het");
        }
        if (booleanValue(voucher.get("discount_type")) && !isVoucherAssigned(String.valueOf(voucher.get("id")), request.getIdKH())) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phieu giam gia khong ap dung cho tai khoan nay");
        }
        if (doubleValue(voucher.get("condition_amount")) > value(request.getTongTien())) {
            double remain = doubleValue(voucher.get("condition_amount")) - value(request.getTongTien());
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Don hang chua du de ap dung phieu giam gia hay mua them " + remain + "d de ap phieu giam gia");
        }
        return new ResponseObject<>(voucherResponse(voucher, value(request.getTongTien())), HttpStatus.OK, "Ap dung phieu giam gia thanh cong");
    }

    @Override
    public ResponseObject<?> getAllApplicablePGG(String idCustomer, Double tongTien) {
        List<Map<String, Object>> rows = new java.util.ArrayList<>(promotionClient.getApplicableVouchers(idCustomer));
        rows.removeIf(row -> voucherUsedByCustomer(String.valueOf(row.get("id")), idCustomer));
        rows.removeIf(row -> intValue(row.get("quantity")) <= 0 || doubleValue(row.get("condition_amount")) > value(tongTien));
        rows = rows.stream()
                .map(row -> voucherResponse(row, value(tongTien)))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        rows.sort((a, b) -> Double.compare(doubleValue(b.get("actualDiscountValue")), doubleValue(a.get("actualDiscountValue"))));
        return new ResponseObject<>(rows, HttpStatus.OK, "Danh sach phieu giam gia hop le");
    }

    private Map<String, Object> voucherResponse(Map<String, Object> voucher, double total) {
        Map<String, Object> row = new java.util.LinkedHashMap<>(voucher);
        row.put("code", voucher.get("code"));
        row.put("name", voucher.get("name"));
        row.put("discountValue", voucher.get("discount_value"));
        row.put("maxDiscountAmount", voucher.get("max_discount_amount"));
        row.put("discountMethod", voucher.get("discount_method"));
        row.put("discountType", voucher.get("discount_type"));
        row.put("actualDiscountValue", discountValue(voucher, total));
        return row;
    }

    @Override
    public ResponseObject<?> getCustomer(String id) {
        Map<String, Object> customer = userClient.getCustomer(id);
        return customer == null || customer.isEmpty()
                ? new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khach hang khong ton tai")
                : new ResponseObject<>(customer, HttpStatus.OK, "Lay khach hang thanh cong");
    }

    private String insertOrder(CheckoutRequest request, int status) {
        String id = UUID.randomUUID().toString();
        String voucherId = voucherId(request.getMaGiamGia());
        String customerId = request.getCustomer() == null || request.getCustomer().isBlank() || "khach le".equalsIgnoreCase(request.getCustomer()) || "khÃƒÂ¡ch lÃ¡ÂºÂ»".equals(request.getCustomer())
                ? null
                : request.getCustomer();
        int paymentMethod = "TIEN_MAT".equals(request.getHinhThucThanhToan())
                ? EntityPhuongThucThanhToan.TIEN_MAT.ordinal()
                : EntityPhuongThucThanhToan.CHUYEN_KHOAN.ordinal();
        jdbcTemplate.update("""
                INSERT INTO orders (id, status, created_date, code, customer_phone, customer_name,
                    shipping_address, discount_amount, order_type, total_amount, total_after_discount, payment_method,
                    email, note, shipping_fee, order_status, voucher_id, customer_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, ACTIVE, System.currentTimeMillis(), generateCode("HD"), request.getSoDienThoai(), request.getHoTen(),
                request.getAddress(), request.getGiamGia(), ONLINE, request.getTongTien(), request.getTongCong(), paymentMethod,
                request.getEmail(), request.getGhiChu(), request.getPhiShip(), status, voucherId, customerId);
        return id;
    }

    private void insertDetailsAndCommitInventory(String orderId, CheckoutRequest request) {
        insertDetails(orderId, request, CHO_XAC_NHAN);
        if (request.getProduct() != null) {
            for (CheckoutProductItem item : request.getProduct()) {
                catalogClient.adjustStock(item.getId(), -value(item.getQuantity()));
            }
        }
    }

    private void insertDetails(String orderId, CheckoutRequest request, int orderSellerStatus) {
        if (request.getProduct() == null) {
            return;
        }
        Map<String, List<LineSnapshot>> grouped = groupItemsBySeller(request);
        for (Map.Entry<String, List<LineSnapshot>> entry : grouped.entrySet()) {
            String orderSellerId = insertOrderSeller(orderId, entry.getKey(), entry.getValue(), orderSellerStatus);
            for (LineSnapshot line : entry.getValue()) {
                CheckoutProductItem item = line.item();
                Double price = line.product().salePrice().doubleValue();
                jdbcTemplate.update("""
                        INSERT INTO order_item (id, status, created_date, code, quantity, sale_price, product_variant_id, order_id, order_seller_id, seller_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, UUID.randomUUID().toString(), ACTIVE, System.currentTimeMillis(), generateCode("HDCT"), item.getQuantity(), price, item.getId(), orderId, orderSellerId, line.sellerId());
            }
        }
    }

    private Map<String, List<LineSnapshot>> groupItemsBySeller(CheckoutRequest request) {
        Map<String, List<LineSnapshot>> grouped = new java.util.LinkedHashMap<>();
        if (request.getProduct() == null) {
            return grouped;
        }
        for (CheckoutProductItem item : request.getProduct()) {
            CatalogVariantSnapshot product = catalogClient.getProductVariant(item.getId());
            String sellerId = product.sellerId();
            if (sellerId == null || sellerId.isBlank()) {
                sellerId = "UNKNOWN_SELLER";
            }
            grouped.computeIfAbsent(sellerId, ignored -> new java.util.ArrayList<>())
                    .add(new LineSnapshot(item, product, sellerId));
        }
        return grouped;
    }

    private String insertOrderSeller(String orderId, String sellerId, List<LineSnapshot> lines, int status) {
        String id = UUID.randomUUID().toString();
        double total = lines.stream()
                .mapToDouble(line -> line.product().salePrice().doubleValue() * value(line.item().getQuantity()))
                .sum();
        Map<String, Object> seller = sellerProfile(sellerId);
        String shopName = stringValue(seller.get("shopName"));
        String sellerSlug = stringValue(seller.get("sellerSlug"));
        jdbcTemplate.update("""
                INSERT INTO order_seller (id, order_id, seller_id, shop_name, seller_slug, total_amount, shipping_fee, discount_amount, total_after_discount, order_status, created_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, orderId, sellerId, shopName == null ? sellerId : shopName, sellerSlug, total, 0D, 0D, total, status, System.currentTimeMillis());
        return id;
    }

    private Map<String, Object> sellerProfile(String sellerId) {
        if (sellerId == null || sellerId.isBlank() || "UNKNOWN_SELLER".equals(sellerId)) {
            return Map.of();
        }
        try {
            Map<String, Object> seller = sellerClient.publicProfile(sellerId);
            return seller == null ? Map.of() : seller;
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private boolean hasStock(CheckoutRequest request) {
        if (request.getProduct() == null) {
            return true;
        }
        for (CheckoutProductItem item : request.getProduct()) {
            Integer stock = catalogClient.getProductVariant(item.getId()).quantity();
            if (stock == null || stock < value(item.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    private void clearCartItemsWhenOutOfStock(CheckoutRequest request) {
        if (request.getProduct() == null) {
            return;
        }
        clearCartItems(request);
    }

    private void clearCartItems(CheckoutRequest request) {
        if (request.getCustomer() == null || request.getProduct() == null || isRetailCustomer(request.getCustomer())) {
            return;
        }
        cartClient.deleteItems(request.getCustomer(), request.getProduct().stream().map(CheckoutProductItem::getId).toList());
    }

    private void clearCartItemsByOrder(String orderId) {
        Map<String, Object> order = getOrder(orderId);
        Object customerId = order.get("customer_id");
        if (customerId == null) {
            return;
        }
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT product_variant_id FROM order_item WHERE order_id = ?", orderId);
        cartClient.deleteItems(String.valueOf(customerId), details.stream().map(detail -> String.valueOf(detail.get("product_variant_id"))).toList());
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
                SELECT COUNT(*) FROM orders
                WHERE voucher_id = ? AND customer_id = ? AND order_status = ?
                """, Integer.class, voucherId, customerId, OrderStatusConstant.HOAN_THANH.ordinal());
        return count != null && count > 0;
    }

    private Map<String, Object> getOrder(String orderId) {
        return jdbcTemplate.queryForMap("SELECT * FROM orders WHERE id = ?", orderId);
    }

    private void insertStatusHistory(String orderId, int status, String note) {
        jdbcTemplate.update("""
                INSERT INTO order_status_history (order_id, status, payment_time, note)
                VALUES (?, ?, ?, ?)
                """, orderId, status, LocalDateTime.now(), note);
    }

    private void insertPayment(String orderId, Double amount, String type, String note) {
        jdbcTemplate.update("""
                INSERT INTO payment_history (amount, payment_time, transaction_code, transaction_type, order_id, note)
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
        if (!booleanValue(voucher.get("discount_method"))) {
            return doubleValue(voucher.get("max_discount_amount"));
        }
        return Math.min(total * doubleValue(voucher.get("discount_value")) / 100, doubleValue(voucher.get("max_discount_amount")));
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
        return "khach le".equalsIgnoreCase(customer) || "khÃƒÂ¡ch lÃ¡ÂºÂ»".equals(customer);
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

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record LineSnapshot(CheckoutProductItem item, CatalogVariantSnapshot product, String sellerId) {
    }
}
