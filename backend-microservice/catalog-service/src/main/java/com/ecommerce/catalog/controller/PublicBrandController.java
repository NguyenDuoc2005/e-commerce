package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.util.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permitall/thuong-hieu")
public class PublicBrandController {

    private final ProductService productService;

    public PublicBrandController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/get-all/thuong-hieu-trang-chu")
    public ResponseEntity<?> getAllThuongHieu(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getThuongHieuTrangChu(request));
    }
}
