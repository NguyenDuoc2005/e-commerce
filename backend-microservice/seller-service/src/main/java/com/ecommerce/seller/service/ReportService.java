package com.ecommerce.seller.service;

import com.ecommerce.seller.client.CatalogClient;
import com.ecommerce.seller.client.NotificationClient;
import com.ecommerce.seller.client.UserClient;
import com.ecommerce.seller.entity.Report;
import com.ecommerce.seller.entity.Review;
import com.ecommerce.seller.entity.Seller;
import com.ecommerce.seller.entity.SellerStatus;
import com.ecommerce.seller.model.CreateReportRequest;
import com.ecommerce.seller.model.ResolveReportRequest;
import com.ecommerce.seller.repository.ReportRepository;
import com.ecommerce.seller.repository.ReviewRepository;
import com.ecommerce.seller.repository.SellerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportService {
    private static final Set<String> TARGETS = Set.of("PRODUCT", "SHOP", "REVIEW", "USER");
    private static final Set<String> REASONS = Set.of("FAKE_PRODUCT", "PROHIBITED_ITEM", "COPYRIGHT", "FAKE_REVIEW", "SCAM", "OFFENSIVE_CONTENT", "OTHER");
    private static final Set<String> ACTIONS = Set.of("PRODUCT_DELISTED", "SHOP_SUSPENDED", "REVIEW_HIDDEN", "WARNING_SENT", "NO_ACTION");
    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "REVIEWING");

    private final ReportRepository reportRepository;
    private final SellerRepository sellerRepository;
    private final ReviewRepository reviewRepository;
    private final CatalogClient catalogClient;
    private final UserClient userClient;
    private final NotificationClient notificationClient;
    private final SellerService sellerService;
    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    public ReportService(ReportRepository reportRepository, SellerRepository sellerRepository,
                         ReviewRepository reviewRepository, CatalogClient catalogClient, UserClient userClient,
                         NotificationClient notificationClient, SellerService sellerService,
                         ReviewService reviewService, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.sellerRepository = sellerRepository;
        this.reviewRepository = reviewRepository;
        this.catalogClient = catalogClient;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
        this.sellerService = sellerService;
        this.reviewService = reviewService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> create(String reporterId, String reporterType, CreateReportRequest request) {
        String targetType = upper(request.getTargetType());
        String reason = upper(request.getReasonCode());
        if (!TARGETS.contains(targetType)) throw new IllegalArgumentException("Loai doi tuong bao cao khong hop le");
        if (!REASONS.contains(reason)) throw new IllegalArgumentException("Ly do bao cao khong hop le");
        targetSnapshot(targetType, request.getTargetId());
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatusIn(
                reporterId, targetType, request.getTargetId(), ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("Ban da co bao cao dang duoc xu ly cho doi tuong nay");
        }
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReporterType(reporterType);
        report.setTargetType(targetType);
        report.setTargetId(request.getTargetId());
        report.setReasonCode(reason);
        report.setDescription(trim(request.getDescription()));
        report.setEvidenceUrls(json(request.getEvidenceUrls()));
        return detail(reportRepository.save(report));
    }

    public List<Map<String, Object>> adminList(String status, String targetType, LocalDate dateFrom, LocalDate dateTo) {
        ZoneId zone = ZoneId.systemDefault();
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> blank(status) || r.getStatus().equalsIgnoreCase(status))
                .filter(r -> blank(targetType) || r.getTargetType().equalsIgnoreCase(targetType))
                .filter(r -> dateFrom == null || !r.getCreatedAt().isBefore(dateFrom.atStartOfDay(zone).toInstant()))
                .filter(r -> dateTo == null || r.getCreatedAt().isBefore(dateTo.plusDays(1).atStartOfDay(zone).toInstant()))
                .map(this::summary).toList();
    }

    public Map<String, Object> adminDetail(String id) { return detail(get(id)); }

    @Transactional
    public Map<String, Object> review(String id, String staffId) {
        Report report = get(id);
        if (!"PENDING".equals(report.getStatus())) throw new IllegalArgumentException("Chi tiep nhan bao cao dang cho xu ly");
        report.setStatus("REVIEWING");
        report.setReviewedByStaffId(staffId);
        report.setReviewedAt(Instant.now());
        return detail(reportRepository.save(report));
    }

    @Transactional
    public Map<String, Object> resolve(String id, String staffId, ResolveReportRequest request) {
        Report report = get(id);
        if (!"REVIEWING".equals(report.getStatus())) throw new IllegalArgumentException("Bao cao chua o trang thai dang xem xet");
        String action = upper(request.getActionTaken());
        if (!ACTIONS.contains(action)) throw new IllegalArgumentException("Hanh dong xu ly khong hop le");
        validateActionTarget(action, report.getTargetType());

        switch (action) {
            case "PRODUCT_DELISTED" -> catalogClient.delistProduct(report.getTargetId());
            case "SHOP_SUSPENDED" -> {
                Seller seller = sellerRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new IllegalArgumentException("Khong tim thay shop"));
                if (seller.getStatus() != SellerStatus.SUSPENDED) sellerService.suspend(seller.getId(), staffId, request.getNote());
            }
            case "REVIEW_HIDDEN" -> reviewService.hideByAdmin(report.getTargetId());
            case "WARNING_SENT" -> sendWarning(report, request.getNote());
            case "NO_ACTION" -> { }
            default -> throw new IllegalArgumentException("Hanh dong xu ly khong hop le");
        }
        report.setActionTaken(action);
        report.setResolutionNote(request.getNote().trim());
        report.setStatus("NO_ACTION".equals(action) ? "DISMISSED" : "ACTION_TAKEN");
        report.setReviewedByStaffId(staffId);
        report.setReviewedAt(Instant.now());
        return detail(reportRepository.save(report));
    }

    private void validateActionTarget(String action, String targetType) {
        if ("PRODUCT_DELISTED".equals(action) && !"PRODUCT".equals(targetType)
                || "SHOP_SUSPENDED".equals(action) && !"SHOP".equals(targetType)
                || "REVIEW_HIDDEN".equals(action) && !"REVIEW".equals(targetType)) {
            throw new IllegalArgumentException("Hanh dong khong phu hop voi doi tuong bao cao");
        }
    }

    private void sendWarning(Report report, String note) {
        String customerId = offenderCustomerId(report);
        Object email = userClient.getCustomer(customerId).get("email");
        if (email == null || String.valueOf(email).isBlank()) throw new IllegalArgumentException("Khong tim thay email nguoi nhan canh bao");
        notificationClient.sendEmail(Map.of("to", String.valueOf(email), "subject", "Canh bao vi pham chinh sach marketplace", "content", note));
    }

    private String offenderCustomerId(Report report) {
        return switch (report.getTargetType()) {
            case "SHOP" -> seller(report.getTargetId()).getOwnerCustomerId();
            case "PRODUCT" -> seller(text(catalogClient.getProduct(report.getTargetId()).get("sellerId"))).getOwnerCustomerId();
            case "REVIEW" -> reviewEntity(report.getTargetId()).getCustomerId();
            case "USER" -> report.getTargetId();
            default -> throw new IllegalArgumentException("Loai doi tuong bao cao khong hop le");
        };
    }

    private Map<String, Object> detail(Report report) {
        Map<String, Object> map = new LinkedHashMap<>(summary(report));
        map.put("description", report.getDescription());
        map.put("evidenceUrls", fromJson(report.getEvidenceUrls()));
        map.put("resolutionNote", report.getResolutionNote());
        map.put("target", targetSnapshot(report.getTargetType(), report.getTargetId()));
        return map;
    }

    private Map<String, Object> summary(Report r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId()); map.put("reporterId", r.getReporterId()); map.put("reporterType", r.getReporterType());
        map.put("targetType", r.getTargetType()); map.put("targetId", r.getTargetId()); map.put("reasonCode", r.getReasonCode());
        map.put("status", r.getStatus()); map.put("actionTaken", r.getActionTaken()); map.put("reviewedByStaffId", r.getReviewedByStaffId());
        map.put("reviewedAt", r.getReviewedAt()); map.put("createdAt", r.getCreatedAt());
        return map;
    }

    private Map<String, Object> targetSnapshot(String type, String id) {
        return switch (type) {
            case "PRODUCT" -> catalogClient.getProduct(id);
            case "SHOP" -> shopMap(seller(id));
            case "REVIEW" -> reviewMap(reviewEntity(id));
            case "USER" -> userMap(id, userClient.getCustomer(id));
            default -> throw new IllegalArgumentException("Loai doi tuong bao cao khong hop le");
        };
    }

    private Map<String, Object> shopMap(Seller seller) {
        return Map.of("id", seller.getId(), "shopName", seller.getShopName(), "sellerSlug", seller.getSellerSlug(), "status", seller.getStatus().name(), "ownerCustomerId", seller.getOwnerCustomerId());
    }
    private Map<String, Object> reviewMap(Review review) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId()); map.put("customerId", review.getCustomerId()); map.put("sellerId", review.getSellerId());
        map.put("productId", review.getProductId()); map.put("comment", review.getComment()); map.put("status", review.getStatus());
        map.put("productRating", review.getProductRating()); map.put("shopRating", review.getShopRating()); map.put("createdAt", review.getCreatedAt());
        return map;
    }
    private Map<String, Object> userMap(String id, Map<String, Object> user) {
        Map<String, Object> map = new LinkedHashMap<>(); map.put("id", id); map.put("name", user.get("name")); map.put("email", user.get("email")); return map;
    }
    private Report get(String id) { return reportRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay bao cao")); }
    private Seller seller(String id) { return sellerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay shop")); }
    private Review reviewEntity(String id) { return reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay danh gia")); }
    private String upper(String value) { return value == null ? "" : value.trim().toUpperCase(); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String trim(String value) { return blank(value) ? null : value.trim(); }
    private String text(Object value) { return value == null ? "" : String.valueOf(value); }
    private String json(List<String> values) { try { return objectMapper.writeValueAsString(values == null ? List.of() : values.stream().filter(v -> v != null && !v.isBlank()).toList()); } catch (JsonProcessingException e) { throw new IllegalArgumentException("Danh sach bang chung khong hop le"); } }
    private List<?> fromJson(String value) { try { return blank(value) ? List.of() : objectMapper.readValue(value, List.class); } catch (JsonProcessingException e) { return new ArrayList<>(); } }
}
