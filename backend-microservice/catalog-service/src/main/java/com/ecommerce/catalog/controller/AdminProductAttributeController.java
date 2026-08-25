package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.model.request.AttributeMergeRequest;
import com.ecommerce.catalog.model.request.AttributeStandardizeRequest;
import com.ecommerce.catalog.service.CatalogAdminService;
import com.ecommerce.catalog.service.CatalogProductService;
import jakarta.validation.Valid;
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
public class AdminProductAttributeController {
    private final CatalogAdminService adminService;
    private final CatalogProductService productService;

    public AdminProductAttributeController(CatalogAdminService adminService, CatalogProductService productService) {
        this.adminService = adminService;
        this.productService = productService;
    }

    @GetMapping
    public Object list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Boolean verified,
                       @RequestParam(required = false) EntityStatus status,
                       @RequestParam(required = false) String categoryId,
                       @RequestParam(required = false) String creatorSellerId) {
        return adminService.definitions(q, verified, status, categoryId, creatorSellerId);
    }

    @PutMapping("/{id}/verify")
    public Object verify(@PathVariable String id, @RequestHeader(value = "X-User-Id", required = false) String actor) {
        return adminService.verifyDefinition(id, actor);
    }

    @PutMapping("/{id}/standardize")
    public Object standardize(@PathVariable String id, @Valid @RequestBody AttributeStandardizeRequest request,
                              @RequestHeader(value = "X-User-Id", required = false) String actor) {
        return adminService.standardize(id, request, actor);
    }

    @PostMapping("/{id}/merge")
    public Object merge(@PathVariable String id, @Valid @RequestBody AttributeMergeRequest request,
                        @RequestHeader(value = "X-User-Id", required = false) String actor) {
        return adminService.mergeDefinition(id, request, actor);
    }

    @PutMapping("/{id}/hide")
    public ResponseEntity<?> hide(@PathVariable String id, @RequestBody(required = false) Map<String, String> body,
                                  @RequestHeader(value = "X-User-Id", required = false) String actor) {
        adminService.hideDefinition(id, body == null ? null : body.get("reason"), actor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/options")
    public Object options(@PathVariable String id) { return adminService.options(id); }

    @GetMapping("/moderation-audits")
    public Object moderationAudits() { return adminService.moderationAudits(); }

    @PutMapping("/options/{optionId}/verify")
    public Object verifyOption(@PathVariable String optionId) { return adminService.verifyOption(optionId); }

    @PostMapping("/options/{optionId}/merge")
    public Object mergeOption(@PathVariable String optionId, @Valid @RequestBody AttributeMergeRequest request) {
        return adminService.mergeOption(optionId, request);
    }

    @PostMapping("/reindex")
    public ResponseEntity<?> reindex() { return ResponseEntity.accepted().body(productService.reindexAll()); }
}
