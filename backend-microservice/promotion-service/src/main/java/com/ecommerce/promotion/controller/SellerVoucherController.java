package com.ecommerce.promotion.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.promotion.model.request.VoucherRequest;
import com.ecommerce.promotion.model.request.VoucherSearchRequest;
import com.ecommerce.promotion.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/vouchers")
@CrossOrigin(origins = "*")
public class SellerVoucherController {

    private final VoucherService voucherService;

    public SellerVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(VoucherSearchRequest request, HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(voucherService.getSellerVouchers(sellerId(servletRequest), request));
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute VoucherRequest request, HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(voucherService.modifySellerVoucher(sellerId(servletRequest), request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id, HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(voucherService.changeSellerVoucherStatus(sellerId(servletRequest), id));
    }

    private String sellerId(HttpServletRequest request) {
        String sellerId = request.getHeader("X-Seller-Id");
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Missing seller context");
        }
        return sellerId;
    }
}
