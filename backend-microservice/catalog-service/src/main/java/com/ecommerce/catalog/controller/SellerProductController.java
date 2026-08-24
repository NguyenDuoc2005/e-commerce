package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.model.request.ProductAggregateRequest;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.service.CatalogProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller/products")
public class SellerProductController {
    private final CatalogProductService service;

    public SellerProductController(CatalogProductService service) { this.service = service; }

    @GetMapping("/categories/tree")
    public Object categories() { return service.categoryTree(); }

    @GetMapping("/categories/{categoryId}/attribute-suggestions")
    public Object suggestions(@PathVariable String categoryId, @RequestParam(defaultValue = "") String q) {
        return service.attributeSuggestions(categoryId, q);
    }

    @GetMapping("/variant-axis-name-suggestions")
    public Object axisSuggestions(@RequestParam(defaultValue = "") String q) { return service.axisNameSuggestions(q); }

    @GetMapping
    public Object list(@RequestHeader("X-Seller-Id") String sellerId, ProductSearchRequest request) {
        return service.sellerProducts(sellerId, request);
    }

    @GetMapping("/{id}")
    public Object detail(@RequestHeader("X-Seller-Id") String sellerId, @PathVariable String id) {
        Map<String, Object> detail = service.detail(id);
        if (!sellerId.equals(detail.get("sellerId"))) throw new SecurityException("SELLER_PRODUCT_FORBIDDEN");
        return detail;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-Seller-Id") String sellerId,
                                    @Valid @RequestBody ProductAggregateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(sellerId, request));
    }

    @PutMapping("/{id}")
    public Object update(@RequestHeader("X-Seller-Id") String sellerId, @PathVariable String id,
                         @Valid @RequestBody ProductAggregateRequest request) {
        return service.update(sellerId, id, request);
    }

    @PutMapping("/{id}/status")
    public Object status(@RequestHeader("X-Seller-Id") String sellerId, @PathVariable String id,
                         @RequestBody Map<String, EntityStatus> body) {
        return service.changeStatus(sellerId, id, body.get("status"));
    }
}
