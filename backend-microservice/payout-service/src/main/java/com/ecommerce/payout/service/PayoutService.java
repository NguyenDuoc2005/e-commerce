package com.ecommerce.payout.service;

import com.ecommerce.payout.entity.CommissionConfig;
import com.ecommerce.payout.entity.SellerReceivable;
import com.ecommerce.payout.entity.SellerWallet;
import com.ecommerce.payout.entity.PayoutAdjustment;
import com.ecommerce.payout.entity.PayoutBatch;
import com.ecommerce.payout.entity.PayoutBatchItem;
import com.ecommerce.payout.model.CommissionConfigRequest;
import com.ecommerce.payout.model.DisputeAdjustmentRequest;
import com.ecommerce.payout.model.ReceivableRequest;
import com.ecommerce.payout.model.PayoutBatchRequest;
import com.ecommerce.payout.model.CommissionLineRequest;
import com.ecommerce.payout.repository.CommissionConfigRepository;
import com.ecommerce.payout.repository.SellerReceivableRepository;
import com.ecommerce.payout.repository.SellerWalletRepository;
import com.ecommerce.payout.repository.PayoutAdjustmentRepository;
import com.ecommerce.payout.repository.PayoutBatchRepository;
import com.ecommerce.payout.repository.PayoutBatchItemRepository;
import com.ecommerce.payout.client.NotificationClient;
import com.ecommerce.payout.client.SellerClient;
import com.ecommerce.payout.client.UserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
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
    private final PayoutBatchRepository batchRepository;
    private final PayoutBatchItemRepository batchItemRepository;

    @Value("${payout.settlement-hold-days:7}")
    private long settlementHoldDays = 7;

    public PayoutService(
            CommissionConfigRepository commissionConfigRepository,
            SellerReceivableRepository receivableRepository,
            SellerWalletRepository walletRepository,
            SellerClient sellerClient,
            UserClient userClient,
            NotificationClient notificationClient,
            PayoutAdjustmentRepository adjustmentRepository,
            PayoutBatchRepository batchRepository,
            PayoutBatchItemRepository batchItemRepository
    ) {
        this.commissionConfigRepository = commissionConfigRepository;
        this.receivableRepository = receivableRepository;
        this.walletRepository = walletRepository;
        this.sellerClient = sellerClient;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
        this.adjustmentRepository = adjustmentRepository;
        this.batchRepository = batchRepository;
        this.batchItemRepository = batchItemRepository;
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
        if (request == null || request.getOrderSellerId() == null || request.getOrderSellerId().isBlank()
                || request.getSellerId() == null || request.getSellerId().isBlank()
                || request.getGrossAmount() == null || request.getGrossAmount() < 0) {
            throw new IllegalArgumentException("Thong tin khoan doi soat khong hop le");
        }
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

    public List<PayoutBatch> payoutBatches() {
        return batchRepository.findAllByOrderByCreatedAtDesc();
    }

    @Scheduled(cron = "${payout.release-cron:0 0 * * * *}")
    @Transactional
    public List<SellerReceivable> releaseEligibleReceivables() {
        Instant now = Instant.now();
        List<SellerReceivable> rows = receivableRepository
                .findEligibleForRelease("PENDING", now,
                        now.minus(Duration.ofDays(Math.max(0, settlementHoldDays))));
        for (SellerReceivable receivable : rows) {
            SellerWallet wallet = sellerWalletForUpdate(receivable.getSellerId());
            double net = amount(receivable.getNetAmount());
            double pending = amount(wallet.getPendingAmount());
            double released = Math.min(net, Math.max(0D, pending));
            wallet.setPendingAmount(pending - released);
            wallet.setAvailableAmount(amount(wallet.getAvailableAmount()) + released);
            wallet.setUpdatedAt(now);
            walletRepository.save(wallet);
            receivable.setReleasedAmount(released);
            receivable.setStatus("AVAILABLE");
            receivableRepository.save(receivable);
        }
        return rows;
    }

    @Transactional
    public PayoutBatch createPayoutBatch(PayoutBatchRequest request, String staffId) {
        List<String> ids = request == null || request.getReceivableIds() == null
                ? List.of()
                : new LinkedHashSet<>(request.getReceivableIds()).stream().filter(id -> id != null && !id.isBlank()).toList();
        if (ids.isEmpty()) throw new IllegalArgumentException("Chua chon khoan doi soat de thanh toan");
        List<SellerReceivable> rows = receivableRepository.findAllByIdForUpdate(ids);
        if (rows.size() != ids.size() || rows.stream().anyMatch(row -> !"AVAILABLE".equals(row.getStatus()))) {
            throw new IllegalArgumentException("Chi duoc thanh toan cac khoan dang AVAILABLE");
        }

        Instant now = Instant.now();
        double total = rows.stream().mapToDouble(row -> amount(row.getReleasedAmount())).sum();
        PayoutBatch batch = new PayoutBatch();
        batch.setItemCount(rows.size());
        batch.setTotalAmount(total);
        batch.setCreatedByStaffId(blankToNull(staffId));
        batch.setNote(request.getNote());
        batch.setPaidAt(now);
        batch = batchRepository.save(batch);

        for (SellerReceivable row : rows) {
            double paid = amount(row.getReleasedAmount());
            SellerWallet wallet = sellerWalletForUpdate(row.getSellerId());
            if (amount(wallet.getAvailableAmount()) < paid) {
                throw new IllegalArgumentException("So du kha dung cua seller khong du");
            }
            wallet.setAvailableAmount(amount(wallet.getAvailableAmount()) - paid);
            wallet.setPaidAmount(amount(wallet.getPaidAmount()) + paid);
            wallet.setUpdatedAt(now);
            walletRepository.save(wallet);

            row.setStatus("PAID");
            row.setPaidAt(now);
            row.setPayoutBatchId(batch.getId());
            receivableRepository.save(row);

            PayoutBatchItem item = new PayoutBatchItem();
            item.setBatchId(batch.getId());
            item.setReceivableId(row.getId());
            item.setSellerId(row.getSellerId());
            item.setAmount(paid);
            batchItemRepository.save(item);
            notifyPaid(row);
        }
        return batch;
    }

    @Transactional
    public SellerReceivable payReceivable(String id) {
        SellerReceivable receivable = receivableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay khoan doi soat"));
        if ("PAID".equals(receivable.getStatus())) {
            return receivable;
        }
        PayoutBatchRequest request = new PayoutBatchRequest();
        request.setReceivableIds(List.of(id));
        createPayoutBatch(request, null);
        return receivableRepository.findById(id).orElseThrow();
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

        SellerWallet wallet = sellerWalletForUpdate(receivable.getSellerId());
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
        } else if ("AVAILABLE".equals(receivable.getStatus())) {
            double oldNet = amount(receivable.getNetAmount());
            double newGross = gross - refund;
            double rate = amount(receivable.getCommissionRate());
            double newCommission = Math.round(newGross * rate) / 100D;
            double newNet = newGross - newCommission;
            double reduction = oldNet - newNet;
            double releasedReduction = Math.min(amount(receivable.getReleasedAmount()), reduction);
            receivable.setGrossAmount(newGross);
            receivable.setCommissionAmount(newCommission);
            receivable.setNetAmount(newNet);
            receivable.setReleasedAmount(amount(receivable.getReleasedAmount()) - releasedReduction);
            if (newGross == 0D) receivable.setStatus("CANCELLED_BY_DISPUTE");
            receivableRepository.save(receivable);
            wallet.setAvailableAmount(amount(wallet.getAvailableAmount()) - releasedReduction);
            wallet.setPendingAmount(amount(wallet.getPendingAmount()) - (reduction - releasedReduction));
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
        CommissionCalculation calculation = calculateCommission(request, gross);
        double rate = calculation.effectiveRate();
        double commission = calculation.amount();
        SellerReceivable receivable = new SellerReceivable();
        receivable.setOrderSellerId(request.getOrderSellerId());
        receivable.setOrderId(request.getOrderId());
        receivable.setSellerId(request.getSellerId());
        receivable.setGrossAmount(gross);
        receivable.setCommissionRate(rate);
        receivable.setCommissionAmount(commission);
        receivable.setNetAmount(gross - commission);
        receivable.setAvailableAt(Instant.now().plus(Duration.ofDays(Math.max(0, settlementHoldDays))));
        SellerReceivable saved = receivableRepository.save(receivable);

        SellerWallet wallet = sellerWalletForUpdate(request.getSellerId());
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

    private CommissionCalculation calculateCommission(ReceivableRequest request, double gross) {
        List<CommissionLineRequest> lines = request.getCommissionLines();
        if (lines == null || lines.isEmpty()) {
            double rate = commissionRate(request.getCategoryId());
            return new CommissionCalculation(rate, Math.round(gross * rate) / 100D);
        }
        double commission = 0D;
        double lineGrossTotal = 0D;
        for (CommissionLineRequest line : lines) {
            if (line == null || line.getGrossAmount() == null || line.getGrossAmount() < 0) {
                throw new IllegalArgumentException("Dong tinh hoa hong khong hop le");
            }
            lineGrossTotal += line.getGrossAmount();
            commission += Math.round(line.getGrossAmount() * commissionRate(line.getCategoryId())) / 100D;
        }
        if (Math.abs(lineGrossTotal - gross) > 0.01D) {
            throw new IllegalArgumentException("Tong dong tinh hoa hong khong khop gross amount");
        }
        double effectiveRate = gross == 0D ? 0D : commission * 100D / gross;
        return new CommissionCalculation(effectiveRate, commission);
    }

    private record CommissionCalculation(double effectiveRate, double amount) {}

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private double amount(Double value) {
        return value == null ? 0D : value;
    }

    private SellerWallet sellerWalletForUpdate(String sellerId) {
        return walletRepository.findBySellerIdForUpdate(sellerId).orElseGet(() -> {
            SellerWallet wallet = new SellerWallet();
            wallet.setSellerId(sellerId);
            return walletRepository.save(wallet);
        });
    }

    private void notifyPaid(SellerReceivable receivable) {
        try {
            Object ownerId = sellerClient.ownerReference(receivable.getSellerId()).get("ownerCustomerId");
            Object email = ownerId == null ? null : userClient.getCustomer(String.valueOf(ownerId)).get("email");
            if (email != null && !String.valueOf(email).isBlank()) {
                notificationClient.sendEmail(java.util.Map.of(
                        "to", String.valueOf(email),
                        "subject", "Khoan doi soat da thanh toan",
                        "content", "So tien thuc nhan " + amount(receivable.getReleasedAmount()) + " VND da duoc ghi nhan thanh toan."
                ));
            }
        } catch (Exception ignored) {
            // Notification availability must not roll back payout state changes.
        }
    }
}
