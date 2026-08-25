package com.ecommerce.seller.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.List;

@FeignClient(name = "order-service", path = "/internal/orders")
public interface OrderClient {
    @GetMapping("/sellers/sold-counts")
    Map<String, Long> sellerSoldCounts(@RequestParam("ids") List<String> sellerIds);

    @GetMapping("/reviews/eligibility")
    Map<String, Object> reviewEligibility(
            @RequestParam("customerId") String customerId,
            @RequestParam("orderSellerId") String orderSellerId,
            @RequestParam("productDetailId") String productDetailId
    );
}
