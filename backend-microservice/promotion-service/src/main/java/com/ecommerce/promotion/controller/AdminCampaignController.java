package com.ecommerce.promotion.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.promotion.entity.PromotionCampaign;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;
import com.ecommerce.promotion.service.PromotionService;
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

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/campaigns")
public class AdminCampaignController {

    private final PromotionService promotionService;

    public AdminCampaignController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute FindPromotionRequest request) {
        return ResponseUtils.createResponseEntity(promotionService.getAll(request));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        return ResponseEntity.ok(promotionService.getProduct());
    }

    @GetMapping("/products/{id}/variants")
    public ResponseEntity<List<Map<String, Object>>> getProductVariants(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.getProductVariants(id));
    }

    @GetMapping("/{id}/product-variants")
    public ResponseEntity<List<Map<String, Object>>> getProductVariantsByCampaign(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.getProductVariantsByCampaign(id));
    }

    @PostMapping
    public ResponseEntity<PromotionCampaign> add(@RequestBody CreatePromotionRequest request) {
        return ResponseEntity.ok(promotionService.add(request));
    }

    @PostMapping("/expired/{id}")
    public ResponseEntity<PromotionCampaign> markExpired(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.updateStatus(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionCampaign> update(@PathVariable String id, @RequestBody UpdatePromotionRequest request) {
        request.setId(id);
        return ResponseEntity.ok(promotionService.update(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionByIdResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.getByIdPromotion(id));
    }
}
