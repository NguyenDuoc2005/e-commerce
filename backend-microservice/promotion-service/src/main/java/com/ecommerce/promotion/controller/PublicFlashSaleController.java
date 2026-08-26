package com.ecommerce.promotion.controller;

import com.ecommerce.promotion.service.FlashSaleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permitall/flash-sales")
public class PublicFlashSaleController {
    private final FlashSaleService service;

    public PublicFlashSaleController(FlashSaleService service) { this.service = service; }

    @GetMapping public Object campaigns() { return service.publicCampaigns(); }
}
