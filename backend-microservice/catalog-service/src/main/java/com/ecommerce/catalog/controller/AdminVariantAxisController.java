package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.service.CatalogAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/product-variant-axes")
public class AdminVariantAxisController {
    private final CatalogAdminService service;
    public AdminVariantAxisController(CatalogAdminService service) { this.service = service; }

    @GetMapping("/insights")
    public Object insights(@RequestParam(defaultValue = "") String q) { return service.axisInsights(q); }

    @PostMapping("/suggestions")
    public Object create(@RequestBody Map<String, String> body) { return service.createAxisSuggestion(body.get("name")); }

    @PutMapping("/suggestions/{id}/verify")
    public Object verify(@PathVariable String id) { return service.verifyAxisSuggestion(id); }

    @PostMapping("/suggestions/{id}/merge")
    public Object merge(@PathVariable String id, @RequestBody Map<String, String> body) {
        return service.mergeAxisSuggestion(id, body.get("targetId"));
    }

    @PutMapping("/suggestions/{id}/hide")
    public ResponseEntity<?> hide(@PathVariable String id) {
        service.hideAxisSuggestion(id);
        return ResponseEntity.noContent().build();
    }
}
