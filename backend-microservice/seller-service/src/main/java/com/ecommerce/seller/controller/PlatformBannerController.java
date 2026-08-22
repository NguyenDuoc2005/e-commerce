package com.ecommerce.seller.controller;

import com.ecommerce.seller.model.PlatformBannerRequest;
import com.ecommerce.seller.service.PlatformBannerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PlatformBannerController {

    private final PlatformBannerService service;

    public PlatformBannerController(PlatformBannerService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/permitall/banners")
    public ResponseEntity<?> publicBanners(@RequestParam(required = false) String position) {
        return ResponseEntity.ok(Map.of("data", service.publicBanners(position)));
    }

    @GetMapping("/api/v1/admin/banners")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(Map.of("data", service.list()));
    }

    @PostMapping("/api/v1/admin/banners")
    public ResponseEntity<?> create(@Valid @RequestBody PlatformBannerRequest request) {
        return ResponseEntity.ok(Map.of("data", service.create(request)));
    }

    @PutMapping("/api/v1/admin/banners/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody PlatformBannerRequest request) {
        return ResponseEntity.ok(Map.of("data", service.update(id, request)));
    }

    @DeleteMapping("/api/v1/admin/banners/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Xoa banner thanh cong"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }
}
