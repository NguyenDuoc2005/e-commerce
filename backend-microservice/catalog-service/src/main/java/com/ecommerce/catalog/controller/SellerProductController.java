package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.catalog.service.DynamicAttributeService;
import com.ecommerce.common.util.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/seller/products")
@CrossOrigin(origins = "*")
public class SellerProductController {

    private final ProductService productService;
    private final DynamicAttributeService dynamicAttributeService;

    public SellerProductController(ProductService productService, DynamicAttributeService dynamicAttributeService) {
        this.productService = productService;
        this.dynamicAttributeService = dynamicAttributeService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(ProductSearchRequest request, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(productService.getSellerAll(request, sellerId));
    }

    @GetMapping("/list-thuong-hieu")
    public ResponseEntity<?> getListBrand() {
        return ResponseUtils.createResponseEntity(productService.getListBrand());
    }

    @GetMapping("/list-xuat-xu")
    public ResponseEntity<?> getListXuatXu() {
        return ResponseUtils.createResponseEntity(productService.getXuatXu());
    }

    @GetMapping("/list-loai-de")
    public ResponseEntity<?> getListSoleType() {
        return ResponseUtils.createResponseEntity(productService.getListSoleType());
    }

    @GetMapping("/list-danh-muc")
    public ResponseEntity<?> getListCategory() {
        return ResponseUtils.createResponseEntity(productService.getListCategory());
    }

    @GetMapping("/list-chat-lieu")
    public ResponseEntity<?> getListMaterial() {
        return ResponseUtils.createResponseEntity(productService.getListMaterial());
    }

    @GetMapping("/categories/{categoryId}/attributes")
    public ResponseEntity<?> getCategoryAttributes(
            @PathVariable String categoryId,
            @RequestParam(required = false) String q,
            @RequestHeader("X-Seller-Id") String sellerId
    ) {
        return ResponseEntity.ok(dynamicAttributeService.suggestions(categoryId, sellerId, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(productService.getSellerProductById(id, sellerId));
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute ProductRequest request, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(productService.modifySellerProduct(request, sellerId));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(productService.changeSellerProductStatus(id, sellerId));
    }
}
