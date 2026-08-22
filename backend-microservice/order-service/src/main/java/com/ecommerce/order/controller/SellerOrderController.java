package com.ecommerce.order.controller;

import com.ecommerce.order.service.SellerOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/orders")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    public SellerOrderController(SellerOrderService sellerOrderService) {
        this.sellerOrderService = sellerOrderService;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String q,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(sellerOrderService.list(sellerId(request), status, q));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpServletRequest request) {
        return ResponseEntity.ok(sellerOrderService.dashboard(sellerId(request)));
    }

    @GetMapping("/{orderSellerId}")
    public ResponseEntity<?> detail(@PathVariable String orderSellerId, HttpServletRequest request) {
        return ResponseEntity.ok(sellerOrderService.detail(sellerId(request), orderSellerId));
    }

    @PostMapping("/{orderSellerId}/confirm")
    public ResponseEntity<?> confirm(@PathVariable String orderSellerId, HttpServletRequest request) {
        return ResponseEntity.ok(sellerOrderService.changeStatus(sellerId(request), orderSellerId, "confirm"));
    }

    @PostMapping("/{orderSellerId}/ready-to-ship")
    public ResponseEntity<?> readyToShip(@PathVariable String orderSellerId, HttpServletRequest request) {
        return ResponseEntity.ok(sellerOrderService.changeStatus(sellerId(request), orderSellerId, "ready-to-ship"));
    }

    @PostMapping("/{orderSellerId}/shipping")
    public ResponseEntity<?> shipping(@PathVariable String orderSellerId, HttpServletRequest request) {
        return ResponseEntity.ok(sellerOrderService.changeStatus(sellerId(request), orderSellerId, "shipping"));
    }

    @PostMapping("/{orderSellerId}/complete")
    public ResponseEntity<?> complete(@PathVariable String orderSellerId, HttpServletRequest request) {
        return ResponseEntity.ok(sellerOrderService.changeStatus(sellerId(request), orderSellerId, "complete"));
    }

    @PostMapping("/{orderSellerId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String orderSellerId, HttpServletRequest request) {
        return ResponseEntity.ok(sellerOrderService.changeStatus(sellerId(request), orderSellerId, "cancel"));
    }

    private String sellerId(HttpServletRequest request) {
        String sellerId = request.getHeader("X-Seller-Id");
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Missing seller context");
        }
        return sellerId;
    }
}
