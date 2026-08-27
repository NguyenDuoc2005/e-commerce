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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock ReportRepository reportRepository;
    @Mock SellerRepository sellerRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock CatalogClient catalogClient;
    @Mock UserClient userClient;
    @Mock NotificationClient notificationClient;
    @Mock SellerService sellerService;
    @Mock ReviewService reviewService;
    ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(reportRepository, sellerRepository, reviewRepository, catalogClient,
                userClient, notificationClient, sellerService, reviewService, new ObjectMapper());
        AtomicInteger ids = new AtomicInteger();
        lenient().when(reportRepository.save(any())).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            if (report.getId() == null) report.setId("report-" + ids.incrementAndGet());
            if (report.getCreatedAt() == null) report.setCreatedAt(Instant.now());
            return report;
        });
    }

    @Test
    void createsReportsForProductShopAndReview() {
        when(catalogClient.getProduct("product-1")).thenReturn(Map.of("id", "product-1", "sellerId", "seller-1", "name", "San pham"));
        when(sellerRepository.findById("seller-1")).thenReturn(Optional.of(seller()));
        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review()));

        assertEquals("PRODUCT", service.create("buyer-1", "BUYER", request("PRODUCT", "product-1")).get("targetType"));
        assertEquals("SHOP", service.create("buyer-2", "BUYER", request("SHOP", "seller-1")).get("targetType"));
        assertEquals("REVIEW", service.create("seller-owner", "SELLER", request("REVIEW", "review-1")).get("targetType"));
        verify(reportRepository, times(3)).save(any());
    }

    @Test
    void resolvesProductReportByDelistingProduct() {
        Report report = report("PRODUCT", "product-1");
        when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));
        when(catalogClient.getProduct("product-1")).thenReturn(Map.of("id", "product-1", "sellerId", "seller-1", "name", "San pham"));
        when(catalogClient.delistProduct("product-1")).thenReturn(Map.of("status", "INACTIVE"));

        Map<String, Object> result = service.resolve("r-1", "staff-1", resolution("PRODUCT_DELISTED"));

        assertEquals("ACTION_TAKEN", result.get("status"));
        verify(catalogClient).delistProduct("product-1");
    }

    @Test
    void resolvesShopReportBySuspendingShop() {
        Report report = report("SHOP", "seller-1");
        Seller seller = seller();
        when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));
        when(sellerRepository.findById("seller-1")).thenReturn(Optional.of(seller));

        service.resolve("r-1", "staff-1", resolution("SHOP_SUSPENDED"));

        verify(sellerService).suspend("seller-1", "staff-1", "Da xac minh vi pham");
    }

    @Test
    void resolvesReviewReportByHidingReview() {
        Report report = report("REVIEW", "review-1");
        when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));
        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review()));
        when(reviewService.hideByAdmin("review-1")).thenReturn(Map.of("status", "HIDDEN"));

        service.resolve("r-1", "staff-1", resolution("REVIEW_HIDDEN"));

        verify(reviewService).hideByAdmin("review-1");
    }

    @Test
    void resolvesUserReportBySendingWarning() {
        Report report = report("USER", "user-1");
        when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));
        when(userClient.getCustomer("user-1")).thenReturn(Map.of("id", "user-1", "name", "User", "email", "user@example.com"));

        service.resolve("r-1", "staff-1", resolution("WARNING_SENT"));

        verify(notificationClient).sendEmail(argThat(mail -> "user@example.com".equals(mail.get("to"))));
    }

    @Test
    void noActionDismissesReport() {
        Report report = report("USER", "user-1");
        when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));
        when(userClient.getCustomer("user-1")).thenReturn(Map.of("id", "user-1", "name", "User", "email", "user@example.com"));

        Map<String, Object> result = service.resolve("r-1", "staff-1", resolution("NO_ACTION"));

        assertEquals("DISMISSED", result.get("status"));
    }

    @Test
    void keepsReportDetailAvailableWhenTargetLookupFails() {
        Report report = report("PRODUCT", "deleted-product");
        when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));
        when(catalogClient.getProduct("deleted-product")).thenThrow(new IllegalArgumentException("missing"));

        Map<String, Object> result = service.adminDetail("r-1");

        Map<?, ?> target = (Map<?, ?>) result.get("target");
        assertEquals(true, target.get("unavailable"));
        assertEquals("deleted-product", target.get("id"));
    }

    @Test
    void rejectsInvalidAdminFilters() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.adminList("UNKNOWN", null, null, null));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.adminList(null, null, LocalDate.of(2026, 8, 27), LocalDate.of(2026, 8, 26)));
        verify(reportRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    private CreateReportRequest request(String type, String id) {
        CreateReportRequest request = new CreateReportRequest(); request.setTargetType(type); request.setTargetId(id);
        request.setReasonCode("OTHER"); request.setDescription("Can kiem tra"); return request;
    }
    private ResolveReportRequest resolution(String action) {
        ResolveReportRequest request = new ResolveReportRequest(); request.setActionTaken(action); request.setNote("Da xac minh vi pham"); return request;
    }
    private Report report(String type, String targetId) {
        Report report = new Report(); report.setId("r-1"); report.setReporterId("buyer-1"); report.setReporterType("BUYER");
        report.setTargetType(type); report.setTargetId(targetId); report.setReasonCode("OTHER"); report.setStatus("REVIEWING"); report.setCreatedAt(Instant.now()); return report;
    }
    private Seller seller() {
        Seller seller = new Seller(); seller.setId("seller-1"); seller.setOwnerCustomerId("owner-1"); seller.setShopName("Demo Shop");
        seller.setSellerSlug("demo-shop"); seller.setStatus(SellerStatus.APPROVED); return seller;
    }
    private Review review() {
        Review review = new Review(); review.setCustomerId("buyer-1"); review.setSellerId("seller-1"); review.setProductId("product-1");
        review.setProductDetailId("variant-1"); review.setOrderSellerId("os-1"); review.setProductRating(1); review.setShopRating(1); review.setStatus("VISIBLE"); return review;
    }
}
