package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "promotion-service", path = "/internal/promotions")
public interface PromotionClient {

    @GetMapping("/vouchers/by-code")
    Map<String, Object> getVoucherByCode(@RequestParam("code") String code);

    @GetMapping("/vouchers/assigned")
    boolean isVoucherAssigned(@RequestParam("voucherId") String voucherId, @RequestParam("customerId") String customerId);

    @PostMapping("/vouchers/decrement")
    void decrementVoucher(@RequestParam("voucherId") String voucherId);

    @GetMapping("/vouchers/applicable")
    List<Map<String, Object>> getApplicableVouchers(@RequestParam("customerId") String customerId);
}
