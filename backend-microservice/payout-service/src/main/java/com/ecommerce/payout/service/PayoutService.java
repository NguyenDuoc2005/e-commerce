package com.ecommerce.payout.service;

import com.ecommerce.payout.entity.CommissionConfig;
import com.ecommerce.payout.entity.SellerReceivable;
import com.ecommerce.payout.entity.SellerWallet;
import com.ecommerce.payout.entity.PayoutAdjustment;
import com.ecommerce.payout.model.CommissionConfigRequest;
import com.ecommerce.payout.model.DisputeAdjustmentRequest;
import com.ecommerce.payout.model.ReceivableRequest;
import com.ecommerce.payout.repository.CommissionConfigRepository;
import com.ecommerce.payout.repository.SellerReceivableRepository;
import com.ecommerce.payout.repository.SellerWalletRepository;
import com.ecommerce.payout.repository.PayoutAdjustmentRepository;
import com.ecommerce.payout.client.NotificationClient;
import com.ecommerce.payout.client.SellerClient;
import com.ecommerce.payout.client.UserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PayoutService {

    private static final double DEFAULT_COMMISSION_RATE = 5D;

    private final CommissionConfigRepository commissionConfigRepository;
    private final SellerReceivableRepository receivableRepository;
    private final SellerWalletRepository walletRepository;
    private final SellerClient sellerClient;
    private final UserClient userClient;
    private final NotificationClient notificationClient;
    private final PayoutAdjustmentRepository adjustmentRepository;

    public PayoutService(
            CommissionConfigRepository commissionConfigRepository,
            SellerReceivableRepository receivableRepository,
            SellerWalletRepository walletRepository,
            SellerClient sellerClient,
            UserClient userClient,
            NotificationClient notificationClient,
            PayoutAdjustmentRepository adjustmentRepository
    ) {
        this.commissionConfigRepository = commissionConfigRepository;
        this.receivableRepository = receivableRepository;
        this.walletRepository = walletRepository;
        this.sellerClient = sellerClient;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
        this.adjustmentRepository = adjustmentRepository;
    }

    public List<CommissionConfig> commissionConfigs() {
        return commissionConfigRepository.findAll();
    }

    public CommissionConfig saveCommissionConfig(CommissionConfigRequest request) {
        if (request.getRatePercent() == null || request.getRatePercent() < 0 || request.getRatePercent() > 100) {
            throw new IllegalArgumentException("Ty le hoa hong khong hop le");
        }
        CommissionConfig config = new CommissionConfig();
        config.setCategoryId(blankToNull(request.getCategoryId()));
        config.setRatePercent(request.getRatePercent());
        config.setActive(true);
        return commissionConfigRepository.save(config);
    }

    @Transactional
    public SellerReceivable createReceivable(ReceivableRequest request) {
        return receivableRepository.findByOrderSellerId(request.getOrderSellerId())
                .orElseGet(() -> createNewReceivable(request));
    }

    public SellerWallet sellerWallet(String sellerId) {
        return walletRepository.findBySellerId(sellerId).orElseGet(() -> {
            SellerWallet wallet = new SellerWallet();
            wallet.setSellerId(sellerId);
            return walletRepository.save(wallet);
        });
    }

    public List<SellerReceivable> sellerReceivables(String sellerId) {
        return receivableRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    public List<SellerReceivable> allReceivables() {
        return receivableRepository.findAll();
    }

    @Transactional
    public SellerReceivable payReceivable(String id) {
        SellerReceivable receivable = receivableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay khoan doi soat"));
        if ("PAID".equals(receivable.getStatus())) {
            return receivable;
        }
        receivable.setStatus("PAID");
        SellerReceivable saved = receivableRepository.save(receivable);
        SellerWallet wallet = sellerWallet(saved.getSellerId());
        double net = saved.getNetAmount() == null ? 0D : saved.getNetAmount();
        wallet.setPendingAmount(Math.max(0D, wallet.getPendingAmount() - net));
        wallet.setPaidAmount(wallet.getPaidAmount() + net);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        notifyPaid(saved);
        return saved;
    }

    @Transactional
    public PayoutAdjustment applyDisputeAdjustment(DisputeAdjustmentRequest request) {
        if (request.getDisputeId() == null || request.getDisputeId().isBlank()
                || request.getRefundAmount() == null || request.getRefundAmount() <= 0) {
            throw new IllegalArgumentException("Thong tin dieu chinh tranh chap khong hop le");
        }
        return adjustmentRepository.findByDisputeId(request.getDisputeId())
                .orElseGet(() -> applyNewDisputeAdjustment(request));
    }

    private PayoutAdjustment applyNewDisputeAdjustment(DisputeAdjustmentRequest request) {
        SellerReceivable receivable = receivableRepository.findByOrderSellerId(request.getOrderSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay khoan doi soat cua don hang"));
        if (!receivable.getSellerId().equals(request.getSellerId())) {
            throw new IllegalArgumentException("Khoan doi soat khong thuoc nha ban");
        }
        double refund = request.getRefundAmount();
        double gross = receivable.getGrossAmount() == null ? 0D : receivable.getGrossAmount();
        if (refund > gross) {
            throw new IllegalArgumentException("So tien hoan vuot qua gia tri don hang");
        }

        SellerWallet wallet = sellerWallet(receivable.getSellerId());
        if ("PENDING".equals(receivable.getStatus())) {
            double oldNet = receivable.getNetAmount() == null ? 0D : receivable.getNetAmount();
            double newGross = gross - refund;
            double rate = receivable.getCommissionRate() == null ? 0D : receivable.getCommissionRate();
            double newCommission = Math.round(newGross * rate) / 100D;
            double newNet = newGross - newCommission;
            receivable.setGrossAmount(newGross);
            receivable.setCommissionAmount(newCommission);
            receivable.setNetAmount(newNet);
            if (newGross == 0D) receivable.setStatus("CANCELLED_BY_DISPUTE");
            receivableRepository.save(receivable);
            wallet.setPendingAmount(wallet.getPendingAmount() - (oldNet - newNet));
        } else if ("PAID".equals(receivable.getStatus())) {
            // A negative pending balance is intentional: future receivables offset a refund
            // that was granted after this seller had already been paid.
            wallet.setPendingAmount(wallet.getPendingAmount() - refund);
        } else {
            throw new IllegalArgumentException("Trang thai khoan doi soat khong cho phep dieu chinh");
        }
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        PayoutAdjustment adjustment = new PayoutAdjustment();
        adjustment.setDisputeId(request.getDisputeId());
        adjustment.setOrderSellerId(request.getOrderSellerId());
        adjustment.setSellerId(request.getSellerId());
        adjustment.setAmount(-refund);
        adjustment.setReason(request.getReason());
        return adjustmentRepository.save(adjustment);
    }

    private SellerReceivable createNewReceivable(ReceivableRequest request) {
        double gross = request.getGrossAmount() == null ? 0D : request.getGrossAmount();
        double rate = commissionRate(request.getCategoryId());
        double commission = Math.round(gross * rate) / 100D;
        SellerReceivable receivable = new SellerReceivable();
        receivable.setOrderSellerId(request.getOrderSellerId());
        receivable.setOrderId(request.getOrderId());
        receivable.setSellerId(request.getSellerId());
        receivable.setGrossAmount(gross);
        receivable.setCommissionRate(rate);
        receivable.setCommissionAmount(commission);
        receivable.setNetAmount(gross - commission);
        SellerReceivable saved = receivableRepository.save(receivable);

        SellerWallet wallet = sellerWallet(request.getSellerId());
        wallet.setPendingAmount(wallet.getPendingAmount() + saved.getNetAmount());
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        return saved;
    }

    private double commissionRate(String categoryId) {
        if (categoryId != null && !categoryId.isBlank()) {
            return commissionConfigRepository.findFirstByCategoryIdAndActiveTrueOrderByCreatedAtDesc(categoryId)
                    .map(CommissionConfig::getRatePercent)
                    .orElseGet(this::defaultCommissionRate);
        }
        return defaultCommissionRate();
    }

    private double defaultCommissionRate() {
        return commissionConfigRepository.findFirstByCategoryIdIsNullAndActiveTrueOrderByCreatedAtDesc()
                .map(CommissionConfig::getRatePercent)
                .orElse(DEFAULT_COMMISSION_RATE);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void notifyPaid(SellerReceivable receivable) {
        try {
            Object ownerId = sellerClient.ownerReference(receivable.getSellerId()).get("ownerCustomerId");
            Object email = ownerId == null ? null : userClient.getCustomer(String.valueOf(ownerId)).get("email");
            if (email != null && !String.valueOf(email).isBlank()) {
                notificationClient.sendEmail(java.util.Map.of(
                        "to", String.valueOf(email),
                        "subject", "Khoan doi soat da thanh toan",
                        "content", "So tien thuc nhan " + receivable.getNetAmount() + " VND da duoc ghi nhan thanh toan."
                ));
            }
        } catch (Exception ignored) {
            // Notification availability must not roll back payout state changes.
        }
    }
}
