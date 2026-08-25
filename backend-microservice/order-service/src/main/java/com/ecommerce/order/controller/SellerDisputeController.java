package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.DisputeMessageRequest;
import com.ecommerce.order.service.DisputeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/disputes")
public class SellerDisputeController {
    private final DisputeService service;
    public SellerDisputeController(DisputeService service) { this.service = service; }
    @GetMapping public ResponseEntity<?> list(@RequestParam(required = false) String status, HttpServletRequest request) {
        return ResponseEntity.ok(service.sellerList(sellerId(request), status));
    }
    @GetMapping("/{id}") public ResponseEntity<?> detail(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.sellerDetail(sellerId(request), id));
    }
    @PostMapping("/{id}/respond") public ResponseEntity<?> respond(@PathVariable String id, @Valid @RequestBody DisputeMessageRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.sellerRespond(sellerId(request), id, body));
    }
    private String sellerId(HttpServletRequest request) {
        String id = request.getHeader("X-Seller-Id");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Missing seller context");
        return id;
    }
}
