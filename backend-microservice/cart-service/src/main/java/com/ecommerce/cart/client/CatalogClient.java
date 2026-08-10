package com.ecommerce.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "catalog-service", path = "/internal/catalog")
public interface CatalogClient {

    @GetMapping("/product-details/{id}")
    Map<String, Object> getProductDetail(@PathVariable("id") String id);
}
