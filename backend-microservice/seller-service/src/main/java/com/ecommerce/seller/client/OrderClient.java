package com.ecommerce.seller.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "order-service", path = "/internal/orders")
public interface OrderClient {
    @GetMapping("/reviews/eligibility")
    Map<String, Object> reviewEligibility(
            @RequestParam("customerId") String customerId,
            @RequestParam("orderSellerId") String orderSellerId,
            @RequestParam("productDetailId") String productDetailId
    );
}
