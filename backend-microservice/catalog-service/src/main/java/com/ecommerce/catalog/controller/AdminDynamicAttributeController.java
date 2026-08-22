package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.constant.AttributeNormalizationStatus;
import com.ecommerce.catalog.model.request.AttributeMergeRequest;
import com.ecommerce.catalog.model.request.AttributeStandardizeRequest;
import com.ecommerce.catalog.service.AdminDynamicAttributeService;
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
@RequestMapping("/api/v1/admin/product-attributes")
public class AdminDynamicAttributeController {

    private final AdminDynamicAttributeService service;

    public AdminDynamicAttributeController(AdminDynamicAttributeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AttributeNormalizationStatus status
    ) {
        return ResponseEntity.ok(service.list(q, status));
    }

    @PutMapping("/{id}/standardize")
    public ResponseEntity<?> standardize(
            @PathVariable String id,
            @RequestBody AttributeStandardizeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actorUserId
    ) {
        return ResponseEntity.ok(service.standardize(id, request, actorUserId));
    }

    @PostMapping("/{id}/merge")
    public ResponseEntity<?> merge(
            @PathVariable String id,
            @RequestBody AttributeMergeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actorUserId
    ) {
        return ResponseEntity.ok(service.merge(id, request, actorUserId));
    }

    @PutMapping("/{id}/hide")
    public ResponseEntity<?> hide(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String actorUserId
    ) {
        service.hide(id, body == null ? null : body.get("reason"), actorUserId);
        return ResponseEntity.noContent().build();
    }
}
