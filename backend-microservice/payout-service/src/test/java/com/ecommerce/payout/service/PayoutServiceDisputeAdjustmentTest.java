package com.ecommerce.payout.service;

import com.ecommerce.payout.client.NotificationClient;
import com.ecommerce.payout.client.SellerClient;
import com.ecommerce.payout.client.UserClient;
import com.ecommerce.payout.entity.PayoutAdjustment;
import com.ecommerce.payout.entity.SellerReceivable;
import com.ecommerce.payout.entity.SellerWallet;
import com.ecommerce.payout.model.DisputeAdjustmentRequest;
import com.ecommerce.payout.repository.CommissionConfigRepository;
import com.ecommerce.payout.repository.PayoutAdjustmentRepository;
import com.ecommerce.payout.repository.PayoutBatchRepository;
import com.ecommerce.payout.repository.PayoutBatchItemRepository;
import com.ecommerce.payout.repository.SellerReceivableRepository;
import com.ecommerce.payout.repository.SellerWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayoutServiceDisputeAdjustmentTest {
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
    void pendingReceivableIsReducedUsingOriginalCommissionRate() {
        SellerReceivable receivable = receivable("PENDING");
        SellerWallet wallet = wallet(950_000D);
        stub(receivable, wallet);

        service.applyDisputeAdjustment(request(200_000D));

        assertEquals(800_000D, receivable.getGrossAmount());
        assertEquals(40_000D, receivable.getCommissionAmount());
        assertEquals(760_000D, receivable.getNetAmount());
        assertEquals(760_000D, wallet.getPendingAmount());
    }

    @Test
    void paidReceivableCreatesNegativeBalanceForFutureOffset() {
        SellerReceivable receivable = receivable("PAID");
        SellerWallet wallet = wallet(0D);
        stub(receivable, wallet);

        PayoutAdjustment result = service.applyDisputeAdjustment(request(200_000D));

        assertEquals(-200_000D, result.getAmount());
        assertEquals(-200_000D, wallet.getPendingAmount());
        assertEquals(1_000_000D, receivable.getGrossAmount());
    }

    private void stub(SellerReceivable receivable, SellerWallet wallet) {
        when(adjustmentRepository.findByDisputeId("d-1")).thenReturn(Optional.empty());
        when(receivableRepository.findByOrderSellerId("os-1")).thenReturn(Optional.of(receivable));
        when(walletRepository.findBySellerIdForUpdate("seller-1")).thenReturn(Optional.of(wallet));
        when(adjustmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private DisputeAdjustmentRequest request(double amount) {
        DisputeAdjustmentRequest request = new DisputeAdjustmentRequest();
        request.setDisputeId("d-1"); request.setOrderSellerId("os-1"); request.setSellerId("seller-1");
        request.setRefundAmount(amount); request.setReason("Dispute refund");
        return request;
    }

    private SellerReceivable receivable(String status) {
        SellerReceivable value = new SellerReceivable();
        value.setOrderSellerId("os-1"); value.setSellerId("seller-1"); value.setGrossAmount(1_000_000D);
        value.setCommissionRate(5D); value.setCommissionAmount(50_000D); value.setNetAmount(950_000D); value.setStatus(status);
        return value;
    }

    private SellerWallet wallet(double pending) {
        SellerWallet value = new SellerWallet(); value.setSellerId("seller-1"); value.setPendingAmount(pending); return value;
    }
}
