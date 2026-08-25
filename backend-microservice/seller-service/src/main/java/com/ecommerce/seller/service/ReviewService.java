package com.ecommerce.seller.service;

import com.ecommerce.seller.client.CatalogClient;
import com.ecommerce.seller.client.OrderClient;
import com.ecommerce.seller.entity.Review;
import com.ecommerce.seller.model.ReviewRequest;
import com.ecommerce.seller.repository.ReviewRepository;
import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.seller.client.NotificationClient;
import com.ecommerce.seller.client.UserClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private static final String VISIBLE = "VISIBLE";

    private final ReviewRepository repository;
    private final OrderClient orderClient;
    private final CatalogClient catalogClient;
    private final ObjectMapper objectMapper;
    private final SellerRepository sellerRepository;
    private final UserClient userClient;
    private final NotificationClient notificationClient;

    public ReviewService(ReviewRepository repository, OrderClient orderClient, CatalogClient catalogClient,
                         ObjectMapper objectMapper, SellerRepository sellerRepository,
                         UserClient userClient, NotificationClient notificationClient) {
        this.repository = repository;
        this.orderClient = orderClient;
        this.catalogClient = catalogClient;
        this.objectMapper = objectMapper;
        this.sellerRepository = sellerRepository;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public Map<String, Object> create(String customerId, ReviewRequest request) {
        if (repository.existsByCustomerIdAndOrderSellerIdAndProductDetailId(
                customerId, request.getOrderSellerId(), request.getProductDetailId())) {
            throw new IllegalArgumentException("San pham trong don hang nay da duoc danh gia");
        }

        Map<String, Object> eligibility = orderClient.reviewEligibility(
                customerId, request.getOrderSellerId(), request.getProductDetailId());
        if (!Boolean.TRUE.equals(eligibility.get("eligible"))) {
            throw new IllegalArgumentException("Chi duoc danh gia san pham da mua trong sub-order hoan thanh");
        }

        Map<String, Object> product = catalogClient.getProductDetail(request.getProductDetailId());
        String sellerId = stringValue(firstNonNull(eligibility.get("seller_id"), eligibility.get("sellerId")));
        String productSellerId = stringValue(product.get("sellerId"));
        String productId = stringValue(product.get("productId"));
        if (sellerId == null || productId == null || !sellerId.equals(productSellerId)) {
            throw new IllegalArgumentException("Thong tin san pham va shop khong hop le");
        }

        Review review = new Review();
        review.setCustomerId(customerId);
        review.setSellerId(sellerId);
        review.setProductId(productId);
        review.setProductDetailId(request.getProductDetailId());
        review.setOrderSellerId(request.getOrderSellerId());
        review.setProductRating(request.getProductRating());
        review.setShopRating(request.getShopRating());
        review.setComment(trimToNull(request.getComment()));
        review.setImageUrls(writeImages(request.getImageUrls()));
        Review saved = repository.save(review);

        Double average = repository.averageProductRating(productId);
        catalogClient.updateRating(productId, average == null ? 0D : average,
                repository.countByProductIdAndStatus(productId, VISIBLE));
        notifySeller(saved);
        return toMap(saved);
    }

    public List<Map<String, Object>> publicReviews(String productId, String sellerId) {
        if (productId != null && !productId.isBlank()) {
            return repository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, VISIBLE).stream().map(this::toMap).toList();
        }
        if (sellerId != null && !sellerId.isBlank()) {
            return repository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, VISIBLE).stream().map(this::toMap).toList();
        }
        throw new IllegalArgumentException("Can productId hoac sellerId de lay danh gia");
    }

    public List<Map<String, Object>> sellerReviews(String sellerId) {
        return repository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream().map(this::toMap).toList();
    }

    public List<Map<String, Object>> customerReviews(String customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> reply(String sellerId, String reviewId, String reply) {
        Review review = repository.findByIdAndSellerId(reviewId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay danh gia cua shop"));
        review.setSellerReply(reply.trim());
        review.setSellerRepliedAt(Instant.now());
        Review saved = repository.save(review);
        notifyCustomer(saved);
        return toMap(saved);
    }

    public double averageShopRating(String sellerId) {
        Double value = repository.averageShopRating(sellerId);
        return value == null ? 0D : Math.round(value * 100D) / 100D;
    }

    public long shopRatingCount(String sellerId) {
        return repository.countBySellerIdAndStatus(sellerId, VISIBLE);
    }

    @Transactional
    public Map<String, Object> hideByAdmin(String reviewId) {
        Review review = repository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay danh gia"));
        if (!"HIDDEN".equals(review.getStatus())) {
            review.setStatus("HIDDEN");
            repository.save(review);
            if (review.getProductId() != null) {
                Double average = repository.averageProductRating(review.getProductId());
                catalogClient.updateRating(review.getProductId(), average == null ? 0D : average,
                        repository.countByProductIdAndStatus(review.getProductId(), VISIBLE));
            }
        }
        return toMap(review);
    }

    private Map<String, Object> toMap(Review review) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", review.getId());
        row.put("customerId", review.getCustomerId());
        row.put("sellerId", review.getSellerId());
        row.put("productId", review.getProductId());
        row.put("productDetailId", review.getProductDetailId());
        row.put("orderSellerId", review.getOrderSellerId());
        row.put("productRating", review.getProductRating());
        row.put("shopRating", review.getShopRating());
        row.put("comment", review.getComment());
        row.put("imageUrls", readImages(review.getImageUrls()));
        row.put("sellerReply", review.getSellerReply());
        row.put("sellerRepliedAt", review.getSellerRepliedAt());
        row.put("status", review.getStatus());
        row.put("createdAt", review.getCreatedAt());
        return row;
    }

    private String writeImages(List<String> images) {
        if (images == null || images.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(images.stream().filter(value -> value != null && !value.isBlank()).toList());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Danh sach anh danh gia khong hop le");
        }
    }

    private List<String> readImages(String images) {
        if (images == null || images.isBlank()) return List.of();
        try {
            return objectMapper.readValue(images, new TypeReference<>() { });
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private static Object firstNonNull(Object... values) {
        for (Object value : values) if (value != null) return value;
        return null;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void notifySeller(Review review) {
        try {
            sellerRepository.findById(review.getSellerId()).ifPresent(seller ->
                    sendToCustomer(seller.getOwnerCustomerId(), "Shop co danh gia moi", "Khach hang vua danh gia san pham cua shop."));
        } catch (Exception ignored) {
            // Notification availability must not roll back review creation.
        }
    }

    private void notifyCustomer(Review review) {
        try {
            sendToCustomer(review.getCustomerId(), "Shop da phan hoi danh gia", review.getSellerReply());
        } catch (Exception ignored) {
            // Notification availability must not roll back seller replies.
        }
    }

    private void sendToCustomer(String customerId, String subject, String content) {
        Object email = userClient.getCustomer(customerId).get("email");
        if (email != null && !String.valueOf(email).isBlank()) {
            notificationClient.sendEmail(Map.of("to", String.valueOf(email), "subject", subject, "content", content));
        }
    }
}
