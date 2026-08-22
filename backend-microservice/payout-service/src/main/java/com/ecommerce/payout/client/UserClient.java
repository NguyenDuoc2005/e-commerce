package com.ecommerce.payout.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {
    @GetMapping("/customers/{id}")
    Map<String, Object> getCustomer(@PathVariable("id") String id);
}
