package com.ecommerce.payout.service;

import com.ecommerce.payout.client.NotificationClient;
import com.ecommerce.payout.client.SellerClient;
import com.ecommerce.payout.client.UserClient;
import com.ecommerce.payout.entity.PayoutBatch;
import com.ecommerce.payout.entity.CommissionConfig;
import com.ecommerce.payout.entity.SellerReceivable;
import com.ecommerce.payout.entity.SellerWallet;
import com.ecommerce.payout.model.PayoutBatchRequest;
import com.ecommerce.payout.model.ReceivableRequest;
import com.ecommerce.payout.model.CommissionLineRequest;
import com.ecommerce.payout.repository.CommissionConfigRepository;
import com.ecommerce.payout.repository.PayoutAdjustmentRepository;
import com.ecommerce.payout.repository.PayoutBatchItemRepository;
import com.ecommerce.payout.repository.PayoutBatchRepository;
import com.ecommerce.payout.repository.SellerReceivableRepository;
import com.ecommerce.payout.repository.SellerWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayoutServiceLifecycleTest {
    @Mock CommissionConfigRepository commissionRepository;
    @Mock SellerReceivableRepository receivableRepository;
    @Mock SellerWalletRepository walletRepository;
    @Mock SellerClient sellerClient;
    @Mock UserClient userClient;
    @Mock NotificationClient notificationClient;
    @Mock PayoutAdjustmentRepository adjustmentRepository;
    @Mock PayoutBatchRepository batchRepository;
    @Mock PayoutBatchItemRepository batchItemRepository;
    PayoutService service;

    @BeforeEach
    void setUp() {
        service = new PayoutService(commissionRepository, receivableRepository, walletRepository,
                sellerClient, userClient, notificationClient, adjustmentRepository, batchRepository, batchItemRepository);
    }

    @Test
    void eligibleReceivableMovesPendingToAvailable() {
        SellerReceivable receivable = receivable("PENDING");
        SellerWallet wallet = wallet(950_000D, 0D, 0D);
        when(receivableRepository.findEligibleForRelease(any(), any(), any()))
                .thenReturn(List.of(receivable));
        when(walletRepository.findBySellerIdForUpdate("seller-1")).thenReturn(Optional.of(wallet));

        List<SellerReceivable> released = service.releaseEligibleReceivables();

        assertEquals(1, released.size());
        assertEquals("AVAILABLE", receivable.getStatus());
        assertEquals(0D, wallet.getPendingAmount());
        assertEquals(950_000D, wallet.getAvailableAmount());
        assertEquals(950_000D, receivable.getReleasedAmount());
    }

    @Test
    void completedOrderCreatesPendingReceivableAndCreditsWallet() {
        when(receivableRepository.findByOrderSellerId("os-1")).thenReturn(Optional.empty());
        when(commissionRepository.findFirstByCategoryIdAndActiveTrueOrderByCreatedAtDesc("category-a"))
                .thenReturn(Optional.of(commission(5D)));
        when(commissionRepository.findFirstByCategoryIdAndActiveTrueOrderByCreatedAtDesc("category-b"))
                .thenReturn(Optional.of(commission(10D)));
        when(receivableRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        SellerWallet wallet = wallet(0D, 0D, 0D);
        when(walletRepository.findBySellerIdForUpdate("seller-1")).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ReceivableRequest request = new ReceivableRequest();
        request.setOrderSellerId("os-1"); request.setOrderId("o-1"); request.setSellerId("seller-1");
        request.setGrossAmount(1_000_000D);
        request.setCommissionLines(List.of(line("category-a", 600_000D), line("category-b", 400_000D)));

        SellerReceivable result = service.createReceivable(request);

        assertEquals("PENDING", result.getStatus());
        assertEquals(7D, result.getCommissionRate());
        assertEquals(70_000D, result.getCommissionAmount());
        assertEquals(930_000D, result.getNetAmount());
        assertEquals(930_000D, wallet.getPendingAmount());
    }

    @Test
    void availableReceivableIsPaidThroughBatchHistory() {
        SellerReceivable receivable = receivable("AVAILABLE");
        receivable.setReleasedAmount(950_000D);
        SellerWallet wallet = wallet(0D, 950_000D, 0D);
        when(receivableRepository.findAllByIdForUpdate(List.of("r-1"))).thenReturn(List.of(receivable));
        when(walletRepository.findBySellerIdForUpdate("seller-1")).thenReturn(Optional.of(wallet));
        when(batchRepository.save(any())).thenAnswer(invocation -> {
            PayoutBatch batch = invocation.getArgument(0);
            batch.setId("b-1");
            return batch;
        });

        PayoutBatchRequest request = new PayoutBatchRequest();
        request.setReceivableIds(List.of("r-1"));
        PayoutBatch batch = service.createPayoutBatch(request, "staff-1");

        assertEquals("b-1", batch.getId());
        assertEquals(950_000D, batch.getTotalAmount());
        assertEquals("PAID", receivable.getStatus());
        assertEquals("b-1", receivable.getPayoutBatchId());
        assertEquals(0D, wallet.getAvailableAmount());
        assertEquals(950_000D, wallet.getPaidAmount());
    }

    private SellerReceivable receivable(String status) {
        SellerReceivable value = new SellerReceivable();
        value.setId("r-1"); value.setSellerId("seller-1"); value.setGrossAmount(1_000_000D);
        value.setCommissionRate(5D); value.setCommissionAmount(50_000D); value.setNetAmount(950_000D);
        value.setStatus(status); value.setAvailableAt(Instant.now().minusSeconds(60));
        return value;
    }

    private SellerWallet wallet(double pending, double available, double paid) {
        SellerWallet value = new SellerWallet(); value.setSellerId("seller-1");
        value.setPendingAmount(pending); value.setAvailableAmount(available); value.setPaidAmount(paid);
        return value;
    }

    private CommissionConfig commission(double rate) {
        CommissionConfig value = new CommissionConfig(); value.setRatePercent(rate); return value;
    }

    private CommissionLineRequest line(String categoryId, double gross) {
        CommissionLineRequest value = new CommissionLineRequest();
        value.setCategoryId(categoryId); value.setGrossAmount(gross); return value;
    }
}
