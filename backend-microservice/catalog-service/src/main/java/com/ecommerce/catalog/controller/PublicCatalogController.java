package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.service.CatalogProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permitall")
public class PublicCatalogController {
    private final CatalogProductService service;
    public PublicCatalogController(CatalogProductService service) { this.service = service; }

    @GetMapping("/products")
    public Object products(ProductSearchRequest request) { return service.publicProducts(request); }

    @GetMapping("/products/{id}")
    public Object detail(@PathVariable String id) {
        Object detail = service.detail(id);
        if (detail instanceof java.util.Map<?, ?> product && product.get("status") != EntityStatus.ACTIVE) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "PRODUCT_NOT_FOUND"));
        }
        return detail;
    }

    @GetMapping("/categories/tree")
    public Object categories() { return service.categoryTree(); }

    @GetMapping("/categories/{categoryId}/attribute-suggestions")
    public Object attributeSuggestions(@PathVariable String categoryId,
                                       @RequestParam(defaultValue = "") String q) {
        return service.attributeSuggestions(categoryId, q);
    }
}
