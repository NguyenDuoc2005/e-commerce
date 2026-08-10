package com.ecommerce.promotion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "catalog-service", path = "/internal/catalog")
public interface CatalogClient {

    @GetMapping("/products")
    List<Map<String, Object>> getProducts();

    @GetMapping("/products/{productId}/details")
    List<Map<String, Object>> getProductDetails(@PathVariable("productId") String productId);

    @GetMapping("/product-details")
    List<Map<String, Object>> getProductDetailsByIds(@RequestParam("ids") List<String> ids);

    @GetMapping("/product-details/{id}")
    Map<String, Object> getProductDetail(@PathVariable("id") String id);

    @GetMapping("/colors")
    List<Map<String, Object>> getColors();

    @GetMapping("/sizes")
    List<Map<String, Object>> getSizes();
}
