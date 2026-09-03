package com.ecommerce.payout.controller;

import com.ecommerce.payout.model.CommissionConfigRequest;
import com.ecommerce.payout.model.PayoutBatchRequest;
import com.ecommerce.payout.service.PayoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @GetMapping("/api/v1/admin/payout/commission-configs")
    public ResponseEntity<?> commissionConfigs() {
        return ResponseEntity.ok(payoutService.commissionConfigs());
    }

    @PostMapping("/api/v1/admin/payout/commission-configs")
    public ResponseEntity<?> saveCommissionConfig(@RequestBody CommissionConfigRequest request) {
        return ResponseEntity.ok(payoutService.saveCommissionConfig(request));
    }

    @GetMapping("/api/v1/admin/payout/receivables")
    public ResponseEntity<?> allReceivables() {
        return ResponseEntity.ok(payoutService.allReceivables());
    }

    @PostMapping("/api/v1/admin/payout/receivables/release-eligible")
    public ResponseEntity<?> releaseEligibleReceivables() {
        return ResponseEntity.ok(payoutService.releaseEligibleReceivables());
    }

    @GetMapping("/api/v1/admin/payout/batches")
    public ResponseEntity<?> payoutBatches() {
        return ResponseEntity.ok(payoutService.payoutBatches());
    }

    @PostMapping("/api/v1/admin/payout/batches")
    public ResponseEntity<?> createPayoutBatch(@RequestBody PayoutBatchRequest body,
                                                @RequestHeader("X-User-Id") String staffId) {
        return ResponseEntity.ok(payoutService.createPayoutBatch(body, staffId));
    }

    @PostMapping("/api/v1/admin/payout/receivables/{id}/pay")
    public ResponseEntity<?> payReceivable(@PathVariable String id) {
        return ResponseEntity.ok(payoutService.payReceivable(id));
    }

    @GetMapping("/api/v1/seller/payout/wallet")
    public ResponseEntity<?> sellerWallet(@RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseEntity.ok(payoutService.sellerWallet(sellerId));
    }

    @GetMapping("/api/v1/seller/payout/receivables")
    public ResponseEntity<?> sellerReceivables(@RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseEntity.ok(payoutService.sellerReceivables(sellerId));
    }
}
