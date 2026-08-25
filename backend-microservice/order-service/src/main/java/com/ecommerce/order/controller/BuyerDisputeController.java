package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.CreateDisputeRequest;
import com.ecommerce.order.model.request.DisputeMessageRequest;
import com.ecommerce.order.service.DisputeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/disputes")
public class BuyerDisputeController {
    private final DisputeService service;
    public BuyerDisputeController(DisputeService service) { this.service = service; }

    @PostMapping public ResponseEntity<?> create(@Valid @RequestBody CreateDisputeRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.createBuyer(userId(request), body));
    }
    @GetMapping public ResponseEntity<?> list(@RequestParam(required = false) String status, HttpServletRequest request) {
        return ResponseEntity.ok(service.buyerList(userId(request), status));
    }
    @GetMapping("/{id}") public ResponseEntity<?> detail(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.buyerDetail(userId(request), id));
    }
    @PostMapping("/{id}/messages") public ResponseEntity<?> message(@PathVariable String id, @Valid @RequestBody DisputeMessageRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.buyerMessage(userId(request), id, body));
    }
    private String userId(HttpServletRequest request) {
        String id = request.getHeader("X-User-Id");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Missing customer context");
        return id;
    }
}
