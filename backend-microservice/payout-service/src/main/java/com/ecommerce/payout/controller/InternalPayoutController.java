package com.ecommerce.payout.controller;

import com.ecommerce.payout.model.ReceivableRequest;
import com.ecommerce.payout.service.PayoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/payout")
public class InternalPayoutController {

    private final PayoutService payoutService;

    public InternalPayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @PostMapping("/receivables")
    public ResponseEntity<?> createReceivable(@RequestBody ReceivableRequest request) {
        return ResponseEntity.ok(payoutService.createReceivable(request));
    }
}
