package com.ecommerce.promotion.controller;

import com.ecommerce.promotion.model.request.FlashSaleCampaignRequest;
import com.ecommerce.promotion.model.request.FlashSaleReviewRequest;
import com.ecommerce.promotion.service.FlashSaleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/flash-sales")
public class AdminFlashSaleController {
    private final FlashSaleService service;

    public AdminFlashSaleController(FlashSaleService service) { this.service = service; }

    @GetMapping public Object campaigns() { return service.adminCampaigns(); }

    @PostMapping
    public Object create(@Valid @RequestBody FlashSaleCampaignRequest request, HttpServletRequest servletRequest) {
        return service.createCampaign(servletRequest.getHeader("X-User-Id"), request);
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable String id, @Valid @RequestBody FlashSaleCampaignRequest request) {
        return service.updateCampaign(id, request);
    }

    @GetMapping("/{id}/registrations")
    public Object registrations(@PathVariable String id) { return service.adminRegistrations(id); }

    @PostMapping("/{campaignId}/registrations/{registrationId}/review")
    public Object review(@PathVariable String campaignId, @PathVariable String registrationId,
                         @Valid @RequestBody FlashSaleReviewRequest request, HttpServletRequest servletRequest) {
        return service.review(campaignId, registrationId, servletRequest.getHeader("X-User-Id"), request);
    }
}
