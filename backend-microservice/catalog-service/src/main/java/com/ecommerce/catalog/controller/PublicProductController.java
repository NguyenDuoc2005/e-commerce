package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.catalog.service.DynamicAttributeService;
import com.ecommerce.common.util.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/permitall/san-pham")
public class PublicProductController {

    private final ProductService productService;
    private final DynamicAttributeService dynamicAttributeService;

    public PublicProductController(ProductService productService, DynamicAttributeService dynamicAttributeService) {
        this.productService = productService;
        this.dynamicAttributeService = dynamicAttributeService;
    }

    @GetMapping("/list-thuong-hieu")
    public ResponseEntity<?> getListBrand() {
        return ResponseUtils.createResponseEntity(productService.getListBrand());
    }

    @GetMapping("/list-xuat-xu")
    public ResponseEntity<?> getListXuatXu() {
        return ResponseUtils.createResponseEntity(productService.getXuatXu());
    }

    @GetMapping("/list-size")
    public ResponseEntity<?> getListSize() {
        return ResponseUtils.createResponseEntity(productService.getListSize());
    }

    @GetMapping("/list-mau")
    public ResponseEntity<?> getListMau() {
        return ResponseUtils.createResponseEntity(productService.getListMau());
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

    @GetMapping("/category-filters")
    public ResponseEntity<?> getCategoryFilters(@RequestParam String categoryId) {
        return ResponseEntity.ok(dynamicAttributeService.publicFilters(categoryId));
    }

    @GetMapping("/get-all/danh-sach-san-pham")
    public ResponseEntity<?> getDanhSachProduct(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getAll(request));
    }

    @GetMapping("/get-all/san-pham-moi")
    public ResponseEntity<?> getProductMoi(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getProductMoi(request));
    }

    @GetMapping("/get-all/san-pham-giam-gia")
    public ResponseEntity<?> getProductGiamGia(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getProductGiamGia(request));
    }
}
