package com.ecommerce.order.client;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.math.BigDecimal;

@FeignClient(name = "catalog-service", path = "/internal/catalog")
public interface CatalogClient {

    @GetMapping("/product-variants/{id}")
    CatalogVariantSnapshot getProductVariant(@PathVariable("id") String id);

    @GetMapping("/product-variants/search")
    List<CatalogVariantSnapshot> searchProductVariants(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice
    );

    @PostMapping("/product-variants/{id}/stock/adjust")
    void adjustStock(@PathVariable("id") String id, @RequestParam("delta") int delta);
}
