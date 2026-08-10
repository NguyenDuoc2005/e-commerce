package com.ecommerce.catalog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "promotion-service", path = "/internal/promotions")
public interface PromotionClient {

    @GetMapping("/discounts/active")
    List<Map<String, Object>> getActiveDiscounts(@RequestParam("productDetailIds") List<String> productDetailIds);
}
