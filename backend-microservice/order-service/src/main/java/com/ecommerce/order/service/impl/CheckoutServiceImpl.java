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
import feign.FeignException;
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

    @Value("${checkout.shipping-fee:30000}")
    private double serverShippingFee;

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
        validateCheckoutRequest(request);
        prepareAuthoritativeTotals(request);
        if (!hasStock(request)) {
            throw new IllegalArgumentException("INSUFFICIENT_STOCK");
        }
        String orderId = insertOrder(request, CHO_XAC_NHAN);
        insertStatusHistory(orderId, CHO_XAC_NHAN, "Don hang da duoc dat va cho duoc wardc nhan.");
        if (!"TIEN_MAT".equals(request.getHinhThucThanhToan())) {
            insertPayment(orderId, request.getTongCong(), "VNPAY".equals(request.getHinhThucThanhToan()) ? "CHUYEN_KHOAN" : "TIEN_MAT", null);
        }
        insertDetails(orderId, request, CHO_XAC_NHAN);
        commitCheckoutSideEffects(request);
        return getOrder(orderId);
    }

    @Override
    @Transactional
    public Map<String, String> createVNPayPaymentUrl(CheckoutRequest request, String ipAddr) {
        validateCheckoutRequest(request);
        prepareAuthoritativeTotals(request);
        validateVNPayConfig();
        if (!hasStock(request)) {
            throw new IllegalArgumentException("INSUFFICIENT_STOCK");
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
        Map<String, Object> existingOrder;
        try {
            existingOrder = jdbcTemplate.queryForMap("SELECT * FROM orders WHERE id = ? FOR UPDATE", orderId);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            return false;
        }
        int currentStatus = intValue(existingOrder.get("order_status"));
        if (currentStatus == CHO_XAC_NHAN) {
            return true;
        }
        if (currentStatus != LUU_TAM) {
            return false;
        }
        long expectedAmount = Math.round(doubleValue(existingOrder.get("total_after_discount")) * 100D);
        if (!String.valueOf(expectedAmount).equals(params.get("vnp_Amount"))) {
            return false;
        }
        jdbcTemplate.update("UPDATE orders SET order_status = ? WHERE id = ?", CHO_XAC_NHAN, orderId);
        jdbcTemplate.update("UPDATE order_seller SET order_status = ? WHERE order_id = ?", CHO_XAC_NHAN, orderId);
        insertStatusHistory(orderId, CHO_XAC_NHAN, "Don hang da duoc dat va cho duoc wardc nhan.");
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT product_variant_id, quantity FROM order_item WHERE order_id = ?", orderId);
        commitPaidOrderSideEffects(existingOrder, details, orderId);
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

    private void insertDetails(String orderId, CheckoutRequest request, int orderSellerStatus) {
        if (request.getProduct() == null) {
            return;
        }
        Map<String, List<LineSnapshot>> grouped = groupItemsBySeller(request);
        double totalItems = grouped.values().stream().flatMap(List::stream)
                .mapToDouble(line -> line.product().salePrice().doubleValue() * value(line.item().getQuantity())).sum();
        String voucherSellerId = voucherSellerId(request.getMaGiamGia());
        for (Map.Entry<String, List<LineSnapshot>> entry : grouped.entrySet()) {
            double sellerTotal = entry.getValue().stream()
                    .mapToDouble(line -> line.product().salePrice().doubleValue() * value(line.item().getQuantity())).sum();
            double shipping = totalItems == 0D ? 0D : request.getPhiShip() * sellerTotal / totalItems;
            double discount = voucherSellerId == null
                    ? (totalItems == 0D ? 0D : request.getGiamGia() * sellerTotal / totalItems)
                    : (voucherSellerId.equals(entry.getKey()) ? request.getGiamGia() : 0D);
            String orderSellerId = insertOrderSeller(orderId, entry.getKey(), entry.getValue(), orderSellerStatus, shipping, discount);
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
            CatalogVariantSnapshot product;
            try {
                product = catalogClient.getProductVariant(item.getId());
            } catch (FeignException exception) {
                if (exception.status() == 400 || exception.status() == 404) {
                    throw new IllegalArgumentException("San pham hoac bien the khong ton tai");
                }
                throw exception;
            }
            String sellerId = product.sellerId();
            if (sellerId == null || sellerId.isBlank()) {
                sellerId = "UNKNOWN_SELLER";
            }
            grouped.computeIfAbsent(sellerId, ignored -> new java.util.ArrayList<>())
                    .add(new LineSnapshot(item, product, sellerId));
        }
        return grouped;
    }

    private String insertOrderSeller(String orderId, String sellerId, List<LineSnapshot> lines, int status,
                                     double shippingFee, double discountAmount) {
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
                """, id, orderId, sellerId, shopName == null ? sellerId : shopName, sellerSlug, total,
                shippingFee, discountAmount, Math.max(0D, total + shippingFee - discountAmount), status, System.currentTimeMillis());
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

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Du lieu dat hang khong hop le");
        }
        if (request.getProduct() == null || request.getProduct().isEmpty()) {
            throw new IllegalArgumentException("Don hang phai co it nhat mot san pham");
        }
        for (CheckoutProductItem item : request.getProduct()) {
            if (item == null || item.getId() == null || item.getId().isBlank()
                    || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("San pham dat hang khong hop le");
            }
        }
        if (request.getHoTen() == null || request.getHoTen().isBlank()
                || request.getSoDienThoai() == null || request.getSoDienThoai().isBlank()
                || request.getAddress() == null || request.getAddress().isBlank()) {
            throw new IllegalArgumentException("Thong tin nguoi nhan chua day du");
        }
        if (value(request.getTongTien()) < 0 || value(request.getPhiShip()) < 0
                || value(request.getGiamGia()) < 0 || value(request.getTongCong()) < 0) {
            throw new IllegalArgumentException("Tong tien don hang khong hop le");
        }
    }

    private void prepareAuthoritativeTotals(CheckoutRequest request) {
        Map<String, List<LineSnapshot>> grouped = groupItemsBySeller(request);
        double itemSubtotal = grouped.values().stream().flatMap(List::stream)
                .mapToDouble(line -> line.product().salePrice().doubleValue() * value(line.item().getQuantity())).sum();
        double discount = 0D;
        if (request.getMaGiamGia() != null && !request.getMaGiamGia().isBlank()) {
            Map<String, Object> voucher = requireApplicableVoucher(request.getMaGiamGia(), request.getCustomer(), grouped, itemSubtotal);
            String sellerId = stringValue(voucher.get("seller_id"));
            double eligibleSubtotal = sellerId == null
                    ? itemSubtotal
                    : grouped.get(sellerId).stream()
                            .mapToDouble(line -> line.product().salePrice().doubleValue() * value(line.item().getQuantity())).sum();
            discount = discountValue(voucher, eligibleSubtotal);
        }
        request.setTongTien(itemSubtotal);
        request.setPhiShip(Math.max(0D, serverShippingFee));
        request.setGiamGia(discount);
        request.setTongCong(Math.max(0D, itemSubtotal + request.getPhiShip() - discount));
    }

    private Map<String, Object> requireApplicableVoucher(String code, String customerId,
                                                          Map<String, List<LineSnapshot>> grouped, double itemSubtotal) {
        Map<String, Object> voucher = promotionClient.getVoucherByCode(code);
        if (voucher == null || voucher.isEmpty() || intValue(voucher.get("status")) != ACTIVE
                || intValue(voucher.get("quantity")) <= 0) {
            throw new IllegalArgumentException("VOUCHER_NOT_AVAILABLE");
        }
        String sellerId = stringValue(voucher.get("seller_id"));
        if (sellerId != null && !grouped.containsKey(sellerId)) {
            throw new IllegalArgumentException("VOUCHER_SELLER_MISMATCH");
        }
        double eligibleSubtotal = sellerId == null ? itemSubtotal : grouped.get(sellerId).stream()
                .mapToDouble(line -> line.product().salePrice().doubleValue() * value(line.item().getQuantity())).sum();
        if (eligibleSubtotal < doubleValue(voucher.get("condition_amount"))) {
            throw new IllegalArgumentException("VOUCHER_MINIMUM_NOT_MET");
        }
        if (booleanValue(voucher.get("discount_type"))
                && (customerId == null || !isVoucherAssigned(String.valueOf(voucher.get("id")), customerId))) {
            throw new IllegalArgumentException("VOUCHER_NOT_ASSIGNED_TO_CUSTOMER");
        }
        if (customerId != null && voucherUsedByCustomer(String.valueOf(voucher.get("id")), customerId)) {
            throw new IllegalArgumentException("VOUCHER_ALREADY_USED");
        }
        return voucher;
    }

    private String voucherSellerId(String code) {
        if (code == null || code.isBlank()) return null;
        Map<String, Object> voucher = promotionClient.getVoucherByCode(code);
        return voucher == null ? null : stringValue(voucher.get("seller_id"));
    }

    private void commitCheckoutSideEffects(CheckoutRequest request) {
        String voucherId = voucherId(request.getMaGiamGia());
        boolean voucherApplied = false;
        List<CheckoutProductItem> adjusted = new java.util.ArrayList<>();
        try {
            if (voucherId != null) {
                promotionClient.decrementVoucher(voucherId);
                voucherApplied = true;
            }
            for (CheckoutProductItem item : request.getProduct()) {
                catalogClient.adjustStock(item.getId(), -value(item.getQuantity()));
                adjusted.add(item);
            }
            clearCartItems(request);
        } catch (RuntimeException exception) {
            compensate(adjusted, voucherId, voucherApplied, exception);
            throw exception;
        }
    }

    private void commitPaidOrderSideEffects(Map<String, Object> order, List<Map<String, Object>> details, String orderId) {
        String voucherId = order.get("voucher_id") == null ? null : String.valueOf(order.get("voucher_id"));
        boolean voucherApplied = false;
        List<CheckoutProductItem> adjusted = new java.util.ArrayList<>();
        try {
            if (voucherId != null) {
                promotionClient.decrementVoucher(voucherId);
                voucherApplied = true;
            }
            for (Map<String, Object> detail : details) {
                CheckoutProductItem item = new CheckoutProductItem();
                item.setId(String.valueOf(detail.get("product_variant_id")));
                item.setQuantity(intValue(detail.get("quantity")));
                catalogClient.adjustStock(item.getId(), -value(item.getQuantity()));
                adjusted.add(item);
            }
            clearCartItemsByOrder(orderId);
        } catch (RuntimeException exception) {
            compensate(adjusted, voucherId, voucherApplied, exception);
            throw exception;
        }
    }

    private void compensate(List<CheckoutProductItem> adjusted, String voucherId, boolean voucherApplied,
                            RuntimeException original) {
        for (int index = adjusted.size() - 1; index >= 0; index--) {
            CheckoutProductItem item = adjusted.get(index);
            try {
                catalogClient.adjustStock(item.getId(), value(item.getQuantity()));
            } catch (RuntimeException compensationFailure) {
                original.addSuppressed(compensationFailure);
            }
        }
        if (voucherApplied) {
            try {
                promotionClient.incrementVoucher(voucherId);
            } catch (RuntimeException compensationFailure) {
                original.addSuppressed(compensationFailure);
            }
        }
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
