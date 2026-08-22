package com.ecommerce.seller.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.seller.entity.SellerStatus;
import com.ecommerce.seller.model.SellerDecisionRequest;
import com.ecommerce.seller.model.SellerRegistrationRequest;
import com.ecommerce.seller.security.JwtPrincipalResolver;
import com.ecommerce.seller.service.SellerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SellerController {

    private final SellerService sellerService;
    private final JwtPrincipalResolver principalResolver;

    public SellerController(SellerService sellerService, JwtPrincipalResolver principalResolver) {
        this.sellerService = sellerService;
        this.principalResolver = principalResolver;
    }

    @PostMapping("/api/v1/sellers/register-shop")
    public ResponseEntity<?> register(@Valid @RequestBody SellerRegistrationRequest request, HttpServletRequest servletRequest) {
        try {
            return ResponseUtils.createResponseEntity(sellerService.register(principalResolver.customerId(servletRequest), request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/api/v1/sellers/my-shop")
    public ResponseEntity<?> myShop(HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(sellerService.myShop(principalResolver.customerId(servletRequest)));
    }

    @GetMapping("/api/v1/permitall/shops/{slug}")
    public ResponseEntity<?> publicShop(@PathVariable String slug) {
        Map<String, Object> shop = sellerService.publicProfileBySlug(slug);
        if (shop.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Khong tim thay shop"));
        }
        return ResponseUtils.createResponseEntity(new com.ecommerce.common.base.ResponseObject<>(shop, HttpStatus.OK, "Lay shop thanh cong"));
    }

    @GetMapping("/api/v1/permitall/shops")
    public ResponseEntity<?> publicShops() {
        return ResponseUtils.createResponseEntity(sellerService.publicShops());
    }

    @GetMapping("/api/v1/permitall/shops/{sellerId}/follow")
    public ResponseEntity<?> followState(@PathVariable String sellerId, HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("data", sellerService.followState(sellerId, principalResolver.customerId(request))));
    }

    @PostMapping("/api/v1/permitall/shops/{sellerId}/follow")
    public ResponseEntity<?> follow(@PathVariable String sellerId, HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("data", sellerService.follow(sellerId, principalResolver.customerId(request))));
    }

    @DeleteMapping("/api/v1/permitall/shops/{sellerId}/follow")
    public ResponseEntity<?> unfollow(@PathVariable String sellerId, HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("data", sellerService.unfollow(sellerId, principalResolver.customerId(request))));
    }

    @GetMapping("/api/v1/admin/sellers")
    public ResponseEntity<?> list(@RequestParam(required = false) SellerStatus status) {
        return ResponseUtils.createResponseEntity(sellerService.list(status));
    }

    @GetMapping("/api/v1/admin/sellers/pending")
    public ResponseEntity<?> pending() {
        return ResponseUtils.createResponseEntity(sellerService.list(SellerStatus.PENDING_APPROVAL));
    }

    @GetMapping("/api/v1/admin/sellers/{id}")
    public ResponseEntity<?> detail(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(sellerService.detail(id));
    }

    @PostMapping("/api/v1/admin/sellers/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id, HttpServletRequest request) {
        return ResponseUtils.createResponseEntity(sellerService.approve(id, principalResolver.staffId(request)));
    }

    @PostMapping("/api/v1/admin/sellers/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id, @RequestBody(required = false) SellerDecisionRequest decision, HttpServletRequest request) {
        return ResponseUtils.createResponseEntity(sellerService.reject(id, principalResolver.staffId(request), decision == null ? null : decision.getReason()));
    }

    @PostMapping("/api/v1/admin/sellers/{id}/suspend")
    public ResponseEntity<?> suspend(@PathVariable String id, @RequestBody(required = false) SellerDecisionRequest decision, HttpServletRequest request) {
        return ResponseUtils.createResponseEntity(sellerService.suspend(id, principalResolver.staffId(request), decision == null ? null : decision.getReason()));
    }

    @PostMapping("/api/v1/admin/sellers/{id}/reopen")
    public ResponseEntity<?> reopen(@PathVariable String id, HttpServletRequest request) {
        return ResponseUtils.createResponseEntity(sellerService.reopen(id, principalResolver.staffId(request)));
    }

    @GetMapping("/api/v1/seller/profile")
    public ResponseEntity<?> sellerProfile(HttpServletRequest request) {
        return ResponseUtils.createResponseEntity(sellerService.sellerProfile(principalResolver.sellerId(request)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
