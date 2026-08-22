package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "catalog-service", path = "/internal/catalog")
public interface CatalogClient {

    @GetMapping("/product-details/{id}")
    Map<String, Object> getProductDetail(@PathVariable("id") String id);

    @GetMapping("/product-details/search")
    List<Map<String, Object>> searchProductDetails(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "idColor", required = false) String idColor,
            @RequestParam(value = "idKichThuoc", required = false) String idKichThuoc,
            @RequestParam(value = "idCategory", required = false) String idCategory,
            @RequestParam(value = "idMaterial", required = false) String idMaterial,
            @RequestParam(value = "idBrand", required = false) String idBrand,
            @RequestParam(value = "idSoleType", required = false) String idSoleType,
            @RequestParam(value = "idSP", required = false) String idSP,
            @RequestParam(value = "priceMin", required = false) Double priceMin,
            @RequestParam(value = "priceMax", required = false) Double priceMax
    );

    @PostMapping("/product-details/{id}/stock/adjust")
    void adjustStock(@PathVariable("id") String id, @RequestParam("delta") int delta);
}
