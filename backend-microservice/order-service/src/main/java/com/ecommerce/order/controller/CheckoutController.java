package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.request.VoucherPaymentRequest;
import com.ecommerce.order.service.CheckoutService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CheckoutRequest order, HttpServletRequest request) {
        if ("VNPAY".equals(order.getHinhThucThanhToan())) {
            return ResponseEntity.ok(checkoutService.createVNPayPaymentUrl(order, request.getRemoteAddr()));
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
        return ResponseEntity.ok(checkoutService.getPhieuGiamGia(request));
    }

    @PostMapping("/pgg/list")
    public ResponseEntity<?> getAllApplicablePGG(@ModelAttribute VoucherPaymentRequest request) {
        return ResponseEntity.ok(checkoutService.getAllApplicablePGG(request.getIdKH(), request.getTongTien()));
    }

    @PostMapping("/khach-hang/{id}")
    public ResponseEntity<?> getKhachHang(@PathVariable String id) {
        return ResponseEntity.ok(checkoutService.getKhachHang(id));
    }
}
