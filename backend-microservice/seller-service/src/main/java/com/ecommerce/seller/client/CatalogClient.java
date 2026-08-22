package com.ecommerce.seller.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "catalog-service", path = "/internal/catalog")
public interface CatalogClient {
    @GetMapping("/product-details/{id}")
    Map<String, Object> getProductDetail(@PathVariable("id") String id);

    @PostMapping("/products/{id}/rating")
    void updateRating(@PathVariable("id") String id,
                      @RequestParam("average") double average,
                      @RequestParam("count") long count);
}
