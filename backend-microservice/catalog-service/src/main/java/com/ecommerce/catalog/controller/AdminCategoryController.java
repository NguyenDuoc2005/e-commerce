package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.service.CatalogCategoryService;
import com.ecommerce.catalog.service.CatalogProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {
    private final CatalogProductService productService;
    private final CatalogCategoryService categoryService;

    public AdminCategoryController(CatalogProductService productService, CatalogCategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public Object tree() { return productService.categoryTree(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable String id, @RequestBody Map<String, Object> request) {
        return categoryService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public Object status(@PathVariable String id, @RequestBody Map<String, EntityStatus> body) {
        return categoryService.status(id, body.get("status"));
    }

    @PutMapping("/{id}/attribute-suggestions")
    public ResponseEntity<?> suggestions(@PathVariable String id, @RequestBody List<Map<String, Object>> request) {
        categoryService.configureSuggestions(id, request);
        return ResponseEntity.noContent().build();
    }
}
