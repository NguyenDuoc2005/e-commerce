package com.ecommerce.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "seller-service", path = "/internal/sellers")
public interface SellerClient {

    @GetMapping("/approved/by-owner")
    Map<String, Object> getApprovedSellerByOwner(@RequestParam("ownerCustomerId") String ownerCustomerId);
}
