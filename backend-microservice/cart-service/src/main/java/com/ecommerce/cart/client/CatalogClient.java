package com.ecommerce.cart.client;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", path = "/internal/catalog")
public interface CatalogClient {

    @GetMapping("/product-variants/{id}")
    CatalogVariantSnapshot getProductVariant(@PathVariable("id") String id);
}
