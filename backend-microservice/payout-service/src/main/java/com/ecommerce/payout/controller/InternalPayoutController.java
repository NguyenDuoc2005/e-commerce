package com.ecommerce.payout.controller;

import com.ecommerce.payout.model.ReceivableRequest;
import com.ecommerce.payout.model.DisputeAdjustmentRequest;
import com.ecommerce.payout.service.PayoutService;
import com.ecommerce.payout.service.EventReplayService;
import com.ecommerce.payout.model.EventReplayRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/payout")
public class InternalPayoutController {

    private final PayoutService payoutService;
    private final EventReplayService replayService;

    public InternalPayoutController(PayoutService payoutService, EventReplayService replayService) {
        this.payoutService = payoutService;
        this.replayService = replayService;
    }

    @PostMapping("/receivables")
    public ResponseEntity<?> createReceivable(@RequestBody ReceivableRequest request) {
        return ResponseEntity.ok(payoutService.createReceivable(request));
    }

    @PostMapping("/dispute-adjustments")
    public ResponseEntity<?> applyDisputeAdjustment(@RequestBody DisputeAdjustmentRequest request) {
        return ResponseEntity.ok(payoutService.applyDisputeAdjustment(request));
    }

    @PostMapping({"/events/replay", "/dlt/replay"})
    public ResponseEntity<?> replay(@RequestBody EventReplayRequest request) {
        return ResponseEntity.ok(java.util.Map.of("status", "REPLAYED", "topic", replayService.replay(request)));
    }
}
