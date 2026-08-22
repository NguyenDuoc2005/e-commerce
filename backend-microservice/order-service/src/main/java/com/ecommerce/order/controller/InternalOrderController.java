package com.ecommerce.order.controller;

import com.ecommerce.order.service.DonMuaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/orders")
public class InternalOrderController {

    private final DonMuaService donMuaService;

    public InternalOrderController(DonMuaService donMuaService) {
        this.donMuaService = donMuaService;
    }

    @GetMapping("/customers/{customerId}/history")
    public List<Map<String, Object>> getCustomerOrderHistory(@PathVariable String customerId) {
        return donMuaService.getCustomerOrderHistory(customerId);
    }

    @GetMapping("/reviews/eligibility")
    public Map<String, Object> reviewEligibility(
            @RequestParam String customerId,
            @RequestParam String orderSellerId,
            @RequestParam String productDetailId
    ) {
        return donMuaService.reviewEligibility(customerId, orderSellerId, productDetailId);
    }
}
