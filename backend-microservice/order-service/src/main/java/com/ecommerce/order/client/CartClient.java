package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "cart-service", path = "/internal/carts")
public interface CartClient {

    @DeleteMapping("/items")
    void deleteItems(@RequestParam("customerId") String customerId, @RequestParam("productDetailIds") List<String> productDetailIds);
}
