package com.ecommerce.order.service;

import com.ecommerce.order.client.PayoutClient;
import com.ecommerce.order.entity.Dispute;
import com.ecommerce.order.entity.DisputeMessage;
import com.ecommerce.order.model.request.CreateDisputeRequest;
import com.ecommerce.order.model.request.DisputeMessageRequest;
import com.ecommerce.order.model.request.ResolveDisputeRequest;
import com.ecommerce.order.repository.DisputeMessageRepository;
import com.ecommerce.order.repository.DisputeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DisputeService {
    private static final int COMPLETED = 4;
    private static final Set<String> TYPES = Set.of("ITEM_NOT_RECEIVED", "ITEM_DAMAGED", "WRONG_ITEM", "NOT_AS_DESCRIBED", "REFUND_REQUEST", "OTHER");
    private static final Set<String> TERMINAL = Set.of("RESOLVED_REFUND_BUYER", "RESOLVED_REJECT_BUYER", "RESOLVED_PARTIAL_REFUND", "CLOSED");

    private final DisputeRepository disputeRepository;
    private final DisputeMessageRepository messageRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PayoutClient payoutClient;
    private final ObjectMapper objectMapper;

    public DisputeService(DisputeRepository disputeRepository, DisputeMessageRepository messageRepository,
                          JdbcTemplate jdbcTemplate, PayoutClient payoutClient, ObjectMapper objectMapper) {
        this.disputeRepository = disputeRepository;
        this.messageRepository = messageRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.payoutClient = payoutClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> createBuyer(String customerId, CreateDisputeRequest request) {
        Map<String, Object> order = orderContext(request.getOrderSellerId());
        requireSame(customerId, text(order.get("customerId")), "Don hang khong thuoc khach hang");
        if (number(order.get("orderStatus")).intValue() != COMPLETED) {
            throw new IllegalArgumentException("Chi co the khieu nai don hang da hoan thanh");
        }
        String type = upper(request.getDisputeType());
        if (!TYPES.contains(type)) throw new IllegalArgumentException("Loai tranh chap khong hop le");
        boolean active = disputeRepository.findByOrderSellerIdOrderByCreatedAtDesc(request.getOrderSellerId()).stream()
                .anyMatch(item -> !TERMINAL.contains(item.getStatus()));
        if (active) throw new IllegalArgumentException("Don hang dang co tranh chap chua ket thuc");
        double total = number(order.get("totalAfterDiscount")).doubleValue();
        if (request.getRequestedAmount() != null && (request.getRequestedAmount() <= 0 || request.getRequestedAmount() > total)) {
            throw new IllegalArgumentException("So tien yeu cau khong hop le");
        }
        Dispute dispute = new Dispute();
        dispute.setOrderSellerId(request.getOrderSellerId());
        dispute.setOrderId(text(order.get("orderId")));
        dispute.setSellerId(text(order.get("sellerId")));
        dispute.setCustomerId(customerId);
        dispute.setRaisedBy("BUYER");
        dispute.setDisputeType(type);
        dispute.setReason(request.getReason().trim());
        dispute.setDescription(request.getDescription());
        dispute.setEvidenceUrls(json(request.getEvidenceUrls()));
        dispute.setRequestedAmount(request.getRequestedAmount());
        return detail(disputeRepository.save(dispute));
    }

    public List<Map<String, Object>> buyerList(String customerId, String status) {
        return filter(disputeRepository.findByCustomerIdOrderByCreatedAtDesc(customerId), status, null, null, null);
    }

    public Map<String, Object> buyerDetail(String customerId, String id) {
        Dispute dispute = get(id);
        requireSame(customerId, dispute.getCustomerId(), "Khong co quyen xem tranh chap");
        return detail(dispute);
    }

    @Transactional
    public Map<String, Object> buyerMessage(String customerId, String id, DisputeMessageRequest request) {
        Dispute dispute = get(id);
        requireSame(customerId, dispute.getCustomerId(), "Khong co quyen cap nhat tranh chap");
        requireActive(dispute);
        saveMessage(dispute, "BUYER", customerId, request);
        return detail(dispute);
    }

    public List<Map<String, Object>> sellerList(String sellerId, String status) {
        return filter(disputeRepository.findBySellerIdOrderByCreatedAtDesc(sellerId), status, null, null, null);
    }

    public Map<String, Object> sellerDetail(String sellerId, String id) {
        Dispute dispute = get(id);
        requireSame(sellerId, dispute.getSellerId(), "Khong co quyen xem tranh chap");
        return detail(dispute);
    }

    @Transactional
    public Map<String, Object> sellerRespond(String sellerId, String id, DisputeMessageRequest request) {
        Dispute dispute = get(id);
        requireSame(sellerId, dispute.getSellerId(), "Khong co quyen phan hoi tranh chap");
        if (!Set.of("OPEN", "SELLER_RESPONDED").contains(dispute.getStatus())) {
            throw new IllegalArgumentException("Trang thai tranh chap khong cho phep phan hoi");
        }
        saveMessage(dispute, "SELLER", sellerId, request);
        if ("OPEN".equals(dispute.getStatus())) {
            dispute.setStatus("SELLER_RESPONDED");
            disputeRepository.save(dispute);
        }
        return detail(dispute);
    }

    public List<Map<String, Object>> adminList(String status, String sellerId, LocalDate dateFrom, LocalDate dateTo) {
        List<Map<String, Object>> result = new ArrayList<>(filter(
                disputeRepository.findAllByOrderByCreatedAtDesc(), status, sellerId, dateFrom, dateTo));
        result.sort(Comparator.<Map<String, Object>>comparingInt(item -> priority(text(item.get("status"))))
                .thenComparing(item -> (Instant) item.get("createdAt"), Comparator.reverseOrder()));
        return result;
    }

    public Map<String, Object> adminDetail(String id) { return detail(get(id)); }

    @Transactional
    public Map<String, Object> takeReview(String staffId, String id) {
        Dispute dispute = get(id);
        if (!Set.of("OPEN", "SELLER_RESPONDED").contains(dispute.getStatus())) {
            throw new IllegalArgumentException("Chi tiep nhan tranh chap moi hoac da duoc nha ban phan hoi");
        }
        dispute.setStatus("UNDER_ADMIN_REVIEW");
        dispute.setResolvedByStaffId(staffId);
        return detail(disputeRepository.save(dispute));
    }

    @Transactional
    public Map<String, Object> adminMessage(String staffId, String id, DisputeMessageRequest request) {
        Dispute dispute = get(id);
        if (!"UNDER_ADMIN_REVIEW".equals(dispute.getStatus())) {
            throw new IllegalArgumentException("Admin can tiep nhan tranh chap truoc khi gui trao doi");
        }
        saveMessage(dispute, "ADMIN", staffId, request);
        return detail(dispute);
    }

    @Transactional
    public Map<String, Object> resolve(String staffId, String id, ResolveDisputeRequest request) {
        Dispute dispute = get(id);
        if (!"UNDER_ADMIN_REVIEW".equals(dispute.getStatus())) {
            throw new IllegalArgumentException("Tranh chap chua o trang thai admin xem xet");
        }
        Map<String, Object> order = orderContext(dispute.getOrderSellerId());
        double total = number(order.get("totalAfterDiscount")).doubleValue();
        String decision = upper(request.getDecision());
        double resolvedAmount;
        switch (decision) {
            case "REFUND_BUYER" -> { resolvedAmount = total; dispute.setStatus("RESOLVED_REFUND_BUYER"); }
            case "REJECT_BUYER" -> { resolvedAmount = 0D; dispute.setStatus("RESOLVED_REJECT_BUYER"); }
            case "PARTIAL_REFUND" -> {
                if (request.getResolvedAmount() == null || request.getResolvedAmount() <= 0 || request.getResolvedAmount() >= total) {
                    throw new IllegalArgumentException("So tien hoan mot phan phai lon hon 0 va nho hon gia tri don");
                }
                resolvedAmount = request.getResolvedAmount();
                dispute.setStatus("RESOLVED_PARTIAL_REFUND");
            }
            default -> throw new IllegalArgumentException("Quyet dinh xu ly khong hop le");
        }
        if (resolvedAmount > 0) {
            payoutClient.applyDisputeAdjustment(Map.of(
                    "disputeId", dispute.getId(), "orderSellerId", dispute.getOrderSellerId(),
                    "sellerId", dispute.getSellerId(), "refundAmount", resolvedAmount,
                    "reason", request.getNote()
            ));
        }
        dispute.setResolvedAmount(resolvedAmount);
        dispute.setResolutionNote(request.getNote());
        dispute.setResolvedByStaffId(staffId);
        dispute.setResolvedAt(Instant.now());
        return detail(disputeRepository.save(dispute));
    }

    @Transactional
    public Map<String, Object> close(String staffId, String id) {
        Dispute dispute = get(id);
        if (!dispute.getStatus().startsWith("RESOLVED_")) throw new IllegalArgumentException("Chi dong tranh chap da co ket qua");
        dispute.setStatus("CLOSED");
        return detail(disputeRepository.save(dispute));
    }

    private List<Map<String, Object>> filter(List<Dispute> source, String status, String sellerId, LocalDate from, LocalDate to) {
        ZoneId zone = ZoneId.systemDefault();
        return source.stream()
                .filter(d -> status == null || status.isBlank() || d.getStatus().equalsIgnoreCase(status))
                .filter(d -> sellerId == null || sellerId.isBlank() || d.getSellerId().equals(sellerId))
                .filter(d -> from == null || !d.getCreatedAt().isBefore(from.atStartOfDay(zone).toInstant()))
                .filter(d -> to == null || d.getCreatedAt().isBefore(to.plusDays(1).atStartOfDay(zone).toInstant()))
                .map(this::summary).toList();
    }

    private Map<String, Object> detail(Dispute dispute) {
        Map<String, Object> result = new LinkedHashMap<>(summary(dispute));
        result.put("evidenceUrls", fromJson(dispute.getEvidenceUrls()));
        result.put("description", dispute.getDescription());
        result.put("resolutionNote", dispute.getResolutionNote());
        result.put("resolvedByStaffId", dispute.getResolvedByStaffId());
        result.put("resolvedAt", dispute.getResolvedAt());
        result.put("order", orderContext(dispute.getOrderSellerId()));
        result.put("items", jdbcTemplate.queryForList("""
                SELECT oi.id, oi.product_variant_id AS productVariantId, oi.quantity,
                       oi.sale_price AS salePrice, oi.name AS productName
                FROM order_item oi WHERE oi.order_seller_id = ? ORDER BY oi.created_date
                """, dispute.getOrderSellerId()));
        result.put("messages", messageRepository.findByDisputeIdOrderByCreatedAtAsc(dispute.getId()).stream().map(this::messageMap).toList());
        return result;
    }

    private Map<String, Object> summary(Dispute d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", d.getId()); map.put("orderSellerId", d.getOrderSellerId()); map.put("orderId", d.getOrderId());
        map.put("sellerId", d.getSellerId()); map.put("customerId", d.getCustomerId()); map.put("raisedBy", d.getRaisedBy());
        map.put("disputeType", d.getDisputeType()); map.put("reason", d.getReason()); map.put("status", d.getStatus());
        map.put("requestedAmount", d.getRequestedAmount()); map.put("resolvedAmount", d.getResolvedAmount());
        map.put("createdAt", d.getCreatedAt()); map.put("updatedAt", d.getUpdatedAt());
        return map;
    }

    private Map<String, Object> messageMap(DisputeMessage m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId()); map.put("senderType", m.getSenderType()); map.put("senderId", m.getSenderId());
        map.put("message", m.getMessage()); map.put("attachmentUrls", fromJson(m.getAttachmentUrls())); map.put("createdAt", m.getCreatedAt());
        return map;
    }

    private Map<String, Object> orderContext(String orderSellerId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT os.id AS orderSellerId, os.order_id AS orderId, os.seller_id AS sellerId,
                       os.shop_name AS shopName, os.total_after_discount AS totalAfterDiscount,
                       os.order_status AS orderStatus, o.customer_id AS customerId, o.code AS orderCode
                FROM order_seller os JOIN orders o ON o.id = os.order_id WHERE os.id = ?
                """, orderSellerId);
        if (rows.isEmpty()) throw new IllegalArgumentException("Khong tim thay don hang cua nha ban");
        return rows.get(0);
    }

    private void saveMessage(Dispute dispute, String senderType, String senderId, DisputeMessageRequest request) {
        DisputeMessage message = new DisputeMessage();
        message.setDisputeId(dispute.getId()); message.setSenderType(senderType); message.setSenderId(senderId);
        message.setMessage(request.getMessage().trim()); message.setAttachmentUrls(json(request.getAttachmentUrls()));
        messageRepository.save(message);
    }
    private void requireActive(Dispute d) { if (TERMINAL.contains(d.getStatus())) throw new IllegalArgumentException("Tranh chap da ket thuc"); }
    private Dispute get(String id) { return disputeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay tranh chap")); }
    private void requireSame(String actual, String expected, String message) { if (actual == null || !actual.equals(expected)) throw new SecurityException(message); }
    private Number number(Object value) { return value instanceof Number n ? n : Double.parseDouble(text(value)); }
    private String text(Object value) { return value == null ? "" : String.valueOf(value); }
    private String upper(String value) { return value == null ? "" : value.trim().toUpperCase(); }
    private int priority(String status) { return switch (status) { case "OPEN" -> 0; case "SELLER_RESPONDED" -> 1; case "UNDER_ADMIN_REVIEW" -> 2; default -> 3; }; }
    private String json(List<String> values) { try { return objectMapper.writeValueAsString(values == null ? List.of() : values.stream().filter(v -> v != null && !v.isBlank()).toList()); } catch (JsonProcessingException e) { throw new IllegalArgumentException("Danh sach tep dinh kem khong hop le"); } }
    private List<?> fromJson(String value) { try { return value == null || value.isBlank() ? List.of() : objectMapper.readValue(value, List.class); } catch (JsonProcessingException e) { return new ArrayList<>(); } }
}
