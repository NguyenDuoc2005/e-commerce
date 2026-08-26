package com.ecommerce.promotion.controller;

import com.ecommerce.promotion.model.request.FlashSaleRegistrationRequest;
import com.ecommerce.promotion.service.FlashSaleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/flash-sales")
public class SellerFlashSaleController {
    private final FlashSaleService service;

    public SellerFlashSaleController(FlashSaleService service) { this.service = service; }

    @GetMapping public Object campaigns(HttpServletRequest request) { return service.sellerCampaigns(sellerId(request)); }
    @GetMapping("/registrations") public Object registrations(HttpServletRequest request) { return service.sellerRegistrations(sellerId(request)); }
    @GetMapping("/variants") public Object variants(HttpServletRequest request) { return service.sellerVariants(sellerId(request)); }

    @PostMapping("/{campaignId}/registrations")
    public Object register(@PathVariable String campaignId, @Valid @RequestBody FlashSaleRegistrationRequest body,
                           HttpServletRequest request) {
        return service.register(campaignId, sellerId(request), body);
    }

    @PostMapping("/{campaignId}/registrations/{registrationId}/withdraw")
    public Object withdraw(@PathVariable String campaignId, @PathVariable String registrationId,
                           HttpServletRequest request) {
        return service.withdraw(campaignId, registrationId, sellerId(request));
    }

    private String sellerId(HttpServletRequest request) {
        String sellerId = request.getHeader("X-Seller-Id");
        if (sellerId == null || sellerId.isBlank()) throw new IllegalArgumentException("Thieu seller context");
        return sellerId;
    }
}
