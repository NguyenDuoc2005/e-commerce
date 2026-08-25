package com.ecommerce.seller.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.seller.entity.Seller;
import com.ecommerce.seller.entity.SellerStatus;
import com.ecommerce.seller.entity.SellerStatusHistory;
import com.ecommerce.seller.entity.ShopFollow;
import com.ecommerce.seller.client.NotificationClient;
import com.ecommerce.seller.client.OrderClient;
import com.ecommerce.seller.client.UserClient;
import com.ecommerce.seller.model.SellerRegistrationRequest;
import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.seller.repository.SellerStatusHistoryRepository;
import com.ecommerce.seller.repository.ShopFollowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerStatusHistoryRepository historyRepository;
    private final ShopFollowRepository followRepository;
    private final ReviewService reviewService;
    private final UserClient userClient;
    private final NotificationClient notificationClient;
    private final OrderClient orderClient;

    public SellerService(
            SellerRepository sellerRepository,
            SellerStatusHistoryRepository historyRepository,
            ShopFollowRepository followRepository,
            ReviewService reviewService,
            UserClient userClient,
            NotificationClient notificationClient,
            OrderClient orderClient
    ) {
        this.sellerRepository = sellerRepository;
        this.historyRepository = historyRepository;
        this.followRepository = followRepository;
        this.reviewService = reviewService;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
        this.orderClient = orderClient;
    }

    @Transactional
    public ResponseObject<?> register(String ownerCustomerId, SellerRegistrationRequest request) {
        if (sellerRepository.findFirstByOwnerCustomerIdOrderByCreatedAtDesc(ownerCustomerId)
                .filter(seller -> seller.getStatus() != SellerStatus.REJECTED && seller.getStatus() != SellerStatus.CLOSED)
                .isPresent()) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "Tai khoan nay da co ho so shop dang hoat dong hoac dang cho duyet");
        }

        String shopName = normalizeRequired(request.getShopName(), "Ten shop khong duoc de trong");
        String slug = request.getSellerSlug() == null || request.getSellerSlug().isBlank()
                ? slugify(shopName)
                : slugify(request.getSellerSlug());
        validateUnique(shopName, slug);

        Seller seller = new Seller();
        seller.setOwnerCustomerId(ownerCustomerId);
        applyRegistration(seller, request, shopName, slug);
        seller.setStatus(SellerStatus.PENDING_APPROVAL);
        Seller saved = sellerRepository.save(seller);
        saveHistory(saved.getId(), null, SellerStatus.PENDING_APPROVAL, ownerCustomerId, "USER", "Dang ky shop");
        return new ResponseObject<>(toMap(saved), HttpStatus.OK, "Dang ky shop thanh cong, vui long cho duyet");
    }

    public ResponseObject<?> myShop(String ownerCustomerId) {
        return sellerRepository.findFirstByOwnerCustomerIdOrderByCreatedAtDesc(ownerCustomerId)
                .map(seller -> new ResponseObject<>(toMap(seller), HttpStatus.OK, "Lay ho so shop thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Tai khoan chua dang ky shop"));
    }

    public ResponseObject<?> sellerProfile(String sellerId) {
        return sellerRepository.findById(sellerId)
                .map(seller -> new ResponseObject<>(toMap(seller), HttpStatus.OK, "Lay ho so shop thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay shop"));
    }

    public ResponseObject<?> list(SellerStatus status) {
        List<Map<String, Object>> rows = (status == null
                ? sellerRepository.findAllByOrderByCreatedAtDesc()
                : sellerRepository.findByStatusOrderByCreatedAtDesc(status))
                .stream()
                .map(this::toMap)
                .toList();
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay danh sach seller thanh cong");
    }

    public ResponseObject<?> publicShops() {
        List<Seller> sellers = sellerRepository.findTop12ByStatusOrderByCreatedAtDesc(SellerStatus.APPROVED);
        Map<String, Long> soldCounts = safeSoldCounts(sellers.stream().map(Seller::getId).toList());
        List<Map<String, Object>> rows = sellers.stream()
                .map(seller -> toPublicMap(seller, soldCounts.getOrDefault(seller.getId(), 0L))).toList();
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay danh sach shop thanh cong");
    }

    public ResponseObject<?> publicProfiles(List<String> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return new ResponseObject<>(List.of(), HttpStatus.OK, "Lay thong tin shop thanh cong");
        }
        List<String> ids = sellerIds.stream().filter(Objects::nonNull).map(String::trim)
                .filter(id -> !id.isBlank()).distinct().toList();
        List<Seller> sellers = sellerRepository.findByIdInAndStatus(ids, SellerStatus.APPROVED);
        Map<String, Long> soldCounts = safeSoldCounts(ids);
        List<Map<String, Object>> rows = sellers.stream()
                .map(seller -> toPublicMap(seller, soldCounts.getOrDefault(seller.getId(), 0L))).toList();
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay thong tin shop thanh cong");
    }

    public ResponseObject<?> detail(String id) {
        return sellerRepository.findById(id)
                .map(seller -> new ResponseObject<>(toMap(seller), HttpStatus.OK, "Lay chi tiet seller thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay seller"));
    }

    @Transactional
    public ResponseObject<?> approve(String id, String staffId) {
        Seller seller = requireSeller(id);
        if (seller.getStatus() != SellerStatus.PENDING_APPROVAL && seller.getStatus() != SellerStatus.REJECTED) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Chi duyet seller dang cho duyet hoac da bi tu choi");
        }
        SellerStatus from = seller.getStatus();
        seller.setStatus(SellerStatus.APPROVED);
        seller.setRejectionReason(null);
        seller.setApprovedByStaffId(staffId);
        seller.setApprovedAt(Instant.now());
        Seller saved = sellerRepository.save(seller);
        saveHistory(id, from, SellerStatus.APPROVED, staffId, "ADMIN", "Duyet seller");
        notifyOwner(saved, "Shop da duoc duyet", "Shop " + saved.getShopName() + " da duoc kich hoat tren marketplace.");
        return new ResponseObject<>(toMap(saved), HttpStatus.OK, "Duyet seller thanh cong");
    }

    @Transactional
    public ResponseObject<?> reject(String id, String staffId, String reason) {
        Seller seller = requireSeller(id);
        if (seller.getStatus() != SellerStatus.PENDING_APPROVAL) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Chi tu choi seller dang cho duyet");
        }
        SellerStatus from = seller.getStatus();
        seller.setStatus(SellerStatus.REJECTED);
        seller.setRejectionReason(reason == null || reason.isBlank() ? "Ho so shop chua hop le" : reason);
        Seller saved = sellerRepository.save(seller);
        saveHistory(id, from, SellerStatus.REJECTED, staffId, "ADMIN", seller.getRejectionReason());
        notifyOwner(saved, "Ho so shop bi tu choi", "Ly do: " + saved.getRejectionReason());
        return new ResponseObject<>(toMap(saved), HttpStatus.OK, "Tu choi seller thanh cong");
    }

    @Transactional
    public ResponseObject<?> suspend(String id, String staffId, String reason) {
        Seller seller = requireSeller(id);
        SellerStatus from = seller.getStatus();
        seller.setStatus(SellerStatus.SUSPENDED);
        Seller saved = sellerRepository.save(seller);
        saveHistory(id, from, SellerStatus.SUSPENDED, staffId, "ADMIN", reason);
        notifyOwner(saved, "Shop tam bi khoa", reason == null || reason.isBlank() ? "Vui long lien he bo phan ho tro." : reason);
        return new ResponseObject<>(toMap(saved), HttpStatus.OK, "Khoa seller thanh cong");
    }

    @Transactional
    public ResponseObject<?> reopen(String id, String staffId) {
        Seller seller = requireSeller(id);
        SellerStatus from = seller.getStatus();
        seller.setStatus(SellerStatus.APPROVED);
        seller.setRejectionReason(null);
        Seller saved = sellerRepository.save(seller);
        saveHistory(id, from, SellerStatus.APPROVED, staffId, "ADMIN", "Mo khoa seller");
        return new ResponseObject<>(toMap(saved), HttpStatus.OK, "Mo khoa seller thanh cong");
    }

    public Map<String, Object> approvedByOwner(String ownerCustomerId) {
        return sellerRepository.findFirstByOwnerCustomerIdAndStatusOrderByCreatedAtDesc(ownerCustomerId, SellerStatus.APPROVED)
                .map(this::toMap)
                .orElseGet(Map::of);
    }

    public Map<String, Map<String, Object>> byOwnerIds(List<String> ownerCustomerIds) {
        if (ownerCustomerIds == null || ownerCustomerIds.isEmpty()) {
            return Map.of();
        }

        List<String> ids = ownerCustomerIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<String, Seller> latestByOwner = new LinkedHashMap<>();
        sellerRepository.findByOwnerCustomerIdIn(ids).stream()
                .sorted(Comparator.comparing(Seller::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .forEach(seller -> latestByOwner.putIfAbsent(seller.getOwnerCustomerId(), seller));

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        ids.forEach(id -> {
            Seller seller = latestByOwner.get(id);
            if (seller != null) {
                result.put(id, toOwnerStatusMap(seller));
            }
        });
        return result;
    }

    public Map<String, Object> publicProfile(String id) {
        return sellerRepository.findById(id)
                .filter(seller -> seller.getStatus() == SellerStatus.APPROVED)
                .map(seller -> toPublicMap(seller, safeSoldCounts(List.of(id)).getOrDefault(id, 0L)))
                .orElseGet(Map::of);
    }

    public Map<String, Object> publicProfileBySlug(String slug) {
        return sellerRepository.findBySellerSlugAndStatus(slug, SellerStatus.APPROVED)
                .map(seller -> toPublicMap(seller, safeSoldCounts(List.of(seller.getId())).getOrDefault(seller.getId(), 0L)))
                .orElseGet(Map::of);
    }

    public Map<String, Object> ownerReference(String id) {
        Seller seller = requireSeller(id);
        return Map.of("ownerCustomerId", seller.getOwnerCustomerId());
    }

    @Transactional
    public Map<String, Object> follow(String sellerId, String customerId) {
        Seller seller = requireApprovedSeller(sellerId);
        if (!followRepository.existsBySellerIdAndCustomerId(sellerId, customerId)) {
            ShopFollow follow = new ShopFollow();
            follow.setSellerId(sellerId);
            follow.setCustomerId(customerId);
            followRepository.save(follow);
        }
        return followState(seller.getId(), customerId);
    }

    @Transactional
    public Map<String, Object> unfollow(String sellerId, String customerId) {
        requireApprovedSeller(sellerId);
        followRepository.deleteBySellerIdAndCustomerId(sellerId, customerId);
        return followState(sellerId, customerId);
    }

    public Map<String, Object> followState(String sellerId, String customerId) {
        requireApprovedSeller(sellerId);
        return Map.of(
                "following", followRepository.existsBySellerIdAndCustomerId(sellerId, customerId),
                "followerCount", followRepository.countBySellerId(sellerId)
        );
    }

    private void applyRegistration(Seller seller, SellerRegistrationRequest request, String shopName, String slug) {
        seller.setShopName(shopName);
        seller.setSellerSlug(slug);
        seller.setDescription(normalizeRequired(request.getDescription(), "Mo ta shop khong duoc de trong"));
        seller.setLogoUrl(blankToNull(request.getLogoUrl()));
        seller.setCoverImageUrl(blankToNull(request.getCoverImageUrl()));
        seller.setPickupAddress(normalizeRequired(request.getPickupAddress(), "Dia chi lay hang khong duoc de trong"));
        seller.setContactPhone(normalizeRequired(request.getContactPhone(), "So dien thoai lien he khong duoc de trong"));
        seller.setIdentityType(normalizeRequired(request.getIdentityType(), "Loai dinh danh khong duoc de trong"));
        seller.setIdentityNumber(normalizeRequired(request.getIdentityNumber(), "So dinh danh khong duoc de trong"));
        seller.setBankName(normalizeRequired(request.getBankName(), "Ten ngan hang khong duoc de trong"));
        seller.setBankAccountNo(normalizeRequired(request.getBankAccountNo(), "So tai khoan ngan hang khong duoc de trong"));
        seller.setBankAccountHolder(normalizeRequired(request.getBankAccountHolder(), "Chu tai khoan ngan hang khong duoc de trong"));
        seller.setMainCategoryId(blankToNull(request.getMainCategoryId()));
    }

    private void validateUnique(String shopName, String slug) {
        if (sellerRepository.existsByShopNameIgnoreCase(shopName)) {
            throw new IllegalArgumentException("Ten shop da ton tai");
        }
        if (sellerRepository.existsBySellerSlug(slug)) {
            throw new IllegalArgumentException("Slug shop da ton tai");
        }
    }

    private Seller requireSeller(String id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay seller"));
    }

    private Seller requireApprovedSeller(String id) {
        return sellerRepository.findById(id)
                .filter(seller -> seller.getStatus() == SellerStatus.APPROVED)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay shop dang hoat dong"));
    }

    private void saveHistory(String sellerId, SellerStatus from, SellerStatus to, String actorId, String actorRole, String reason) {
        SellerStatusHistory history = new SellerStatusHistory();
        history.setSellerId(sellerId);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedByUserId(actorId);
        history.setChangedByRole(actorRole);
        history.setReason(reason);
        historyRepository.save(history);
    }

    private Map<String, Object> toMap(Seller seller) {
        Map<String, Object> row = toPublicMap(seller, 0L);
        row.put("ownerCustomerId", seller.getOwnerCustomerId());
        row.put("pickupAddress", seller.getPickupAddress());
        row.put("contactPhone", seller.getContactPhone());
        row.put("identityType", seller.getIdentityType());
        row.put("identityNumber", seller.getIdentityNumber());
        row.put("bankName", seller.getBankName());
        row.put("bankAccountNo", seller.getBankAccountNo());
        row.put("bankAccountHolder", seller.getBankAccountHolder());
        row.put("mainCategoryId", seller.getMainCategoryId());
        row.put("rejectionReason", seller.getRejectionReason());
        row.put("approvedByStaffId", seller.getApprovedByStaffId());
        row.put("approvedAt", seller.getApprovedAt());
        row.put("createdAt", seller.getCreatedAt());
        row.put("updatedAt", seller.getUpdatedAt());
        return row;
    }

    private Map<String, Object> toOwnerStatusMap(Seller seller) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sellerId", seller.getId());
        row.put("shopName", seller.getShopName());
        row.put("sellerSlug", seller.getSellerSlug());
        row.put("status", seller.getStatus() == null ? null : seller.getStatus().name());
        row.put("approvedAt", seller.getApprovedAt());
        row.put("createdAt", seller.getCreatedAt());
        return row;
    }

    private Map<String, Object> toPublicMap(Seller seller, long soldCount) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", seller.getId());
        row.put("shopName", seller.getShopName());
        row.put("sellerSlug", seller.getSellerSlug());
        row.put("description", seller.getDescription());
        row.put("logoUrl", seller.getLogoUrl());
        row.put("coverImageUrl", seller.getCoverImageUrl());
        row.put("status", seller.getStatus() == null ? null : seller.getStatus().name());
        row.put("followerCount", followRepository.countBySellerId(seller.getId()));
        row.put("rating", reviewService.averageShopRating(seller.getId()));
        row.put("ratingCount", reviewService.shopRatingCount(seller.getId()));
        row.put("soldCount", soldCount);
        return row;
    }

    private Map<String, Long> safeSoldCounts(List<String> sellerIds) {
        try {
            Map<String, Long> result = orderClient.sellerSoldCounts(sellerIds);
            return result == null ? Map.of() : result;
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (normalized.isBlank()) {
            normalized = "shop";
        }
        return normalized;
    }

    private void notifyOwner(Seller seller, String subject, String content) {
        try {
            Object email = userClient.getCustomer(seller.getOwnerCustomerId()).get("email");
            if (email != null && !String.valueOf(email).isBlank()) {
                notificationClient.sendEmail(Map.of("to", String.valueOf(email), "subject", subject, "content", content));
            }
        } catch (Exception ignored) {
            // Notification availability must not roll back seller state transitions.
        }
    }
}
