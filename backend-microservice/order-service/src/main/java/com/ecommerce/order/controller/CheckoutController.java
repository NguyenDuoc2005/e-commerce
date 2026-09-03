package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.request.VoucherPaymentRequest;
import com.ecommerce.order.service.CheckoutService;
import com.ecommerce.order.service.CheckoutIdempotencyConflictException;
import com.ecommerce.order.service.CheckoutIdempotencyCoordinator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CheckoutIdempotencyCoordinator idempotencyCoordinator;

    public CheckoutController(CheckoutService checkoutService, CheckoutIdempotencyCoordinator idempotencyCoordinator) {
        this.checkoutService = checkoutService;
        this.idempotencyCoordinator = idempotencyCoordinator;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @RequestBody CheckoutRequest order,
            @RequestHeader("X-User-Id") String customerId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest request
    ) {
        order.setCustomer(customerId);
        if ("VNPAY".equals(order.getHinhThucThanhToan())) {
            return ResponseEntity.ok(checkoutService.createVNPayPaymentUrl(order, request.getRemoteAddr()));
        }
        if ("TIEN_MAT".equalsIgnoreCase(order.getHinhThucThanhToan())) {
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Idempotency-Key bat buoc cho checkout COD"));
            }
            return ResponseEntity.ok(idempotencyCoordinator.execute(customerId, idempotencyKey.trim(),
                    () -> checkoutService.createOrder(order)));
        }
        return ResponseEntity.ok(checkoutService.createOrder(order));
    }

    @GetMapping("/vnpay-return")
    public RedirectView handleVNPayReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value != null && value.length > 0 && value[0] != null && !value[0].isEmpty()) {
                params.put(key, value[0]);
            }
        });
        boolean success = checkoutService.handleVNPayReturn(params);
        return new RedirectView(success ? "http://localhost:6688/thanh-toan-thanh-cong" : "http://localhost:6688/trang-chu");
    }

    @PostMapping("/pgg")
    public ResponseEntity<?> getPGG(@ModelAttribute VoucherPaymentRequest request) {
        return ResponseEntity.ok(checkoutService.getVoucher(request));
    }

    @PostMapping("/pgg/list")
    public ResponseEntity<?> getAllApplicablePGG(@ModelAttribute VoucherPaymentRequest request) {
        return ResponseEntity.ok(checkoutService.getAllApplicablePGG(request.getIdKH(), request.getTongTien()));
    }

    @PostMapping("/khach-hang/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable String id) {
        return ResponseEntity.ok(checkoutService.getCustomer(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleInvalidCheckout(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(CheckoutIdempotencyConflictException.class)
    public ResponseEntity<?> handleIdempotencyConflict(CheckoutIdempotencyConflictException exception) {
        return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
    }
}
