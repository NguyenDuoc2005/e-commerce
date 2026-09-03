package com.ecommerce.order.service;

import com.ecommerce.order.client.PayoutClient;
import com.ecommerce.order.entity.Dispute;
import com.ecommerce.order.model.request.CreateDisputeRequest;
import com.ecommerce.order.model.request.DisputeMessageRequest;
import com.ecommerce.order.model.request.ResolveDisputeRequest;
import com.ecommerce.order.repository.DisputeMessageRepository;
import com.ecommerce.order.repository.DisputeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {
    @Mock DisputeRepository disputeRepository;
    @Mock DisputeMessageRepository messageRepository;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock PayoutClient payoutClient;
    DisputeService service;

    @BeforeEach
    void setUp() {
        service = new DisputeService(disputeRepository, messageRepository, jdbcTemplate, payoutClient, new ObjectMapper());
    }

    @Test
    void buyerCannotOpenDisputeBeforeOrderIsCompleted() {
        stubOrder(3);
        CreateDisputeRequest request = new CreateDisputeRequest();
        request.setOrderSellerId("os-1"); request.setDisputeType("OTHER"); request.setReason("Can ho tro");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createBuyer("buyer-1", request));

        assertTrue(error.getMessage().contains("hoan thanh"));
        verify(disputeRepository, never()).save(any());
    }

    @Test
    void sellerCannotReadAnotherSellersDispute() {
        Dispute dispute = dispute("UNDER_ADMIN_REVIEW");
        when(disputeRepository.findById("d-1")).thenReturn(Optional.of(dispute));
        assertThrows(SecurityException.class, () -> service.sellerDetail("seller-2", "d-1"));
    }

    @Test
    void adminPartialRefundCreatesPayoutAdjustmentAndResolvesCase() {
        Dispute dispute = dispute("UNDER_ADMIN_REVIEW");
        when(disputeRepository.findById("d-1")).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByDisputeIdOrderByCreatedAtAsc("d-1")).thenReturn(List.of());
        when(payoutClient.applyDisputeAdjustment(any())).thenReturn(Map.of("status", "APPLIED"));
        stubOrder(4);

        ResolveDisputeRequest request = new ResolveDisputeRequest();
        request.setDecision("PARTIAL_REFUND"); request.setResolvedAmount(250_000D); request.setNote("Hang hu hong mot phan");
        Map<String, Object> result = service.resolve("staff-1", "d-1", request);

        assertEquals("RESOLVED_PARTIAL_REFUND", result.get("status"));
        assertEquals(250_000D, result.get("resolvedAmount"));
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(payoutClient).applyDisputeAdjustment(captor.capture());
        assertEquals("d-1", captor.getValue().get("disputeId"));
        assertEquals(250_000D, captor.getValue().get("refundAmount"));
    }

    @Test
    void adminCanTakeReviewImmediatelyForOpenDispute() {
        Dispute dispute = dispute("OPEN");
        when(disputeRepository.findById("d-1")).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByDisputeIdOrderByCreatedAtAsc("d-1")).thenReturn(List.of());
        stubOrder(4);

        Map<String, Object> result = service.takeReview("staff-1", "d-1");

        assertEquals("UNDER_ADMIN_REVIEW", result.get("status"));
        assertEquals("staff-1", result.get("resolvedByStaffId"));
    }

    @Test
    void adminCanMessageCaseAfterTakingReview() {
        Dispute dispute = dispute("UNDER_ADMIN_REVIEW");
        when(disputeRepository.findById("d-1")).thenReturn(Optional.of(dispute));
        when(messageRepository.findByDisputeIdOrderByCreatedAtAsc("d-1")).thenReturn(List.of());
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        stubOrder(4);
        DisputeMessageRequest request = new DisputeMessageRequest();
        request.setMessage("Vui long bo sung bang chung");

        service.adminMessage("staff-1", "d-1", request);

        verify(messageRepository).save(argThat(message -> "ADMIN".equals(message.getSenderType())
                && "staff-1".equals(message.getSenderId())
                && "Vui long bo sung bang chung".equals(message.getMessage())));
    }

    @Test
    void adminListCanSortStreamResultWithoutUnsupportedOperation() {
        Dispute resolved = dispute("RESOLVED_REJECT_BUYER");
        resolved.setId("d-2");
        resolved.setCreatedAt(Instant.now().plusSeconds(10));
        Dispute open = dispute("OPEN");
        when(disputeRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(resolved, open));

        List<Map<String, Object>> result = service.adminList(null, null, null, null);

        assertEquals(List.of("OPEN", "RESOLVED_REJECT_BUYER"),
                result.stream().map(item -> item.get("status")).toList());
    }

    private void stubOrder(int status) {
        when(jdbcTemplate.queryForList(anyString(), eq("os-1"))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("FROM order_seller")) {
                return List.of(Map.of("orderSellerId", "os-1", "orderId", "o-1", "sellerId", "seller-1",
                        "shopName", "Demo Shop", "totalAmount", 1_000_000D, "discountAmount", 0D,
                        "totalAfterDiscount", 1_000_000D, "orderStatus", status,
                        "customerId", "buyer-1", "orderCode", "ORD-1"));
            }
            return List.of();
        });
    }

    private Dispute dispute(String status) {
        Dispute dispute = new Dispute();
        dispute.setId("d-1"); dispute.setOrderSellerId("os-1"); dispute.setOrderId("o-1");
        dispute.setSellerId("seller-1"); dispute.setCustomerId("buyer-1"); dispute.setRaisedBy("BUYER");
        dispute.setDisputeType("ITEM_DAMAGED"); dispute.setReason("Hang hu hong"); dispute.setStatus(status);
        dispute.setCreatedAt(Instant.now()); dispute.setUpdatedAt(Instant.now());
        return dispute;
    }
}
