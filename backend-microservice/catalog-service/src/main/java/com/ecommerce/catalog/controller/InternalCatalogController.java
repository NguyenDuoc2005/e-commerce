package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.service.CatalogProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/internal/catalog")
public class InternalCatalogController {
    private final CatalogProductService service;
    public InternalCatalogController(CatalogProductService service) { this.service = service; }

    @GetMapping("/products")
    public Object products() { return service.activeProductSummaries(); }

    @GetMapping("/products/{productId}/variants")
    public Object variants(@PathVariable String productId) { return service.productVariantSnapshots(productId); }

    @GetMapping("/product-variants")
    public Object variants(@RequestParam List<String> ids) { return service.variantSnapshots(ids); }

    @GetMapping("/product-variants/search")
    public Object searchVariants(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return service.searchVariantSnapshots(q, status, productId, minPrice, maxPrice);
    }

    @GetMapping("/product-variants/{variantId}")
    public Object variant(@PathVariable String variantId) { return service.variantSnapshot(variantId); }

    @PostMapping("/product-variants/{variantId}/stock/adjust")
    public Object adjust(@PathVariable String variantId, @RequestParam int delta) { return service.adjustStock(variantId, delta); }
}
