package com.ecommerce.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "order-service", path = "/internal/orders")
public interface OrderClient {

    @GetMapping("/customers/{customerId}/history")
    List<Map<String, Object>> getCustomerOrderHistory(@PathVariable("customerId") String customerId);
}
