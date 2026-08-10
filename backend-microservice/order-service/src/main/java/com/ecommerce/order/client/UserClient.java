package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {

    @GetMapping("/customers/{id}")
    Map<String, Object> getCustomer(@PathVariable("id") String id);

    @GetMapping("/customers")
    List<Map<String, Object>> searchCustomers(@RequestParam(value = "q", required = false) String q);

    @PostMapping("/customers")
    Map<String, Object> createCustomer(@RequestParam("ten") String ten, @RequestParam("sdt") String sdt);
}
