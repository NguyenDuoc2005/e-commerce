package com.ecommerce.order.controller;

import com.ecommerce.order.service.DonMuaService;
import com.ecommerce.order.service.OrderCheckoutSagaExecutor;
import com.ecommerce.order.service.SellerOrderService;
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
    private final SellerOrderService sellerOrderService;
    private final OrderCheckoutSagaExecutor sagaExecutor;

    public InternalOrderController(DonMuaService donMuaService, SellerOrderService sellerOrderService,
                                   OrderCheckoutSagaExecutor sagaExecutor) {
        this.donMuaService = donMuaService;
        this.sellerOrderService = sellerOrderService;
        this.sagaExecutor = sagaExecutor;
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

    @GetMapping("/sellers/sold-counts")
    public Map<String, Long> sellerSoldCounts(@RequestParam List<String> ids) {
        return sellerOrderService.soldCounts(ids);
    }

    @org.springframework.web.bind.annotation.PostMapping("/saga-steps/{id}/retry-compensation")
    public Map<String, Object> retryCompensation(@PathVariable String id) {
        return sagaExecutor.retryCompensationManually(id);
    }
}
