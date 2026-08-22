package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "payout-service")
public interface PayoutClient {

    @PostMapping("/internal/payout/receivables")
    Map<String, Object> createReceivable(@RequestBody Map<String, Object> request);
}
