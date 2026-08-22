package com.ecommerce.promotion.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.promotion.entity.PromotionCampaign;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/seller/promotions")
public class SellerPromotionController {

    private final PromotionService promotionService;

    public SellerPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute FindPromotionRequest request, HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(promotionService.getSellerAll(sellerId(servletRequest), request));
    }

    @PostMapping
    public ResponseEntity<PromotionCampaign> add(@RequestBody CreatePromotionRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(promotionService.addSeller(sellerId(servletRequest), request));
    }

    @PostMapping("/expired/{id}")
    public ResponseEntity<PromotionCampaign> markExpired(@PathVariable String id, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(promotionService.updateSellerStatus(sellerId(servletRequest), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionCampaign> update(@PathVariable String id, @RequestBody UpdatePromotionRequest request, HttpServletRequest servletRequest) {
        request.setId(id);
        return ResponseEntity.ok(promotionService.updateSeller(sellerId(servletRequest), request));
    }

    private String sellerId(HttpServletRequest request) {
        String sellerId = request.getHeader("X-Seller-Id");
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Missing seller context");
        }
        return sellerId;
    }
}
