package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.ProductDetailRequest;
import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.catalog.service.ProductDetailService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/product-variants")
@CrossOrigin(origins = "*")
public class SellerProductVariantController {

    private final ProductDetailService service;

    public SellerProductVariantController(ProductDetailService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAll(ProductDetailSearchRequest request, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(service.getSellerAll(request, sellerId));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(service.changeSellerProductStatus(id, sellerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(service.getSellerSPCTById(id, sellerId));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(service.getSellerDetailSPCT(id, sellerId));
    }

    @GetMapping("/list-mau")
    public ResponseEntity<?> getListColor() {
        return ResponseUtils.createResponseEntity(service.getListColor());
    }

    @GetMapping("/list-size")
    public ResponseEntity<?> getListSize() {
        return ResponseUtils.createResponseEntity(service.getListSize());
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute ProductDetailRequest request, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(service.modifySellerProduct(request, sellerId));
    }

    @PostMapping("update")
    public ResponseEntity<?> update(@ModelAttribute ProductDetailRequest request, @RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(service.updateSellerProduct(request, sellerId));
    }

    @GetMapping("/list-sp")
    public ResponseEntity<?> getListProduct(@RequestHeader("X-Seller-Id") String sellerId) {
        return ResponseUtils.createResponseEntity(service.getSellerListProduct(sellerId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> lowStock(
            @RequestParam(defaultValue = "5") Integer threshold,
            @RequestHeader("X-Seller-Id") String sellerId
    ) {
        return ResponseUtils.createResponseEntity(service.getSellerLowStock(sellerId, threshold));
    }
}
