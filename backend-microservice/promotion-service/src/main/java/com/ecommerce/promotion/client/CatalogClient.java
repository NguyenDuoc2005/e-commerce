package com.ecommerce.promotion.client;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
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

    @GetMapping("/products/{productId}/variants")
    List<CatalogVariantSnapshot> getProductVariants(@PathVariable("productId") String productId);

    @GetMapping("/product-variants")
    List<CatalogVariantSnapshot> getProductVariantsByIds(@RequestParam("ids") List<String> ids);

    @GetMapping("/product-variants/{id}")
    CatalogVariantSnapshot getProductVariant(@PathVariable("id") String id);
}
