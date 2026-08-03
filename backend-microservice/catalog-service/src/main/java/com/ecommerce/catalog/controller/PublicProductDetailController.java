package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.catalog.service.ProductDetailService;
import com.ecommerce.common.util.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permitall/san-pham-chi-tiet")
public class PublicProductDetailController {

    private final ProductDetailService service;

    public PublicProductDetailController(ProductDetailService service) {
        this.service = service;
    }

    @GetMapping("/get-all/san-pham-chi-tiet")
    public ResponseEntity<?> getAll(ProductDetailSearchRequest request) {
        request.setStatus("1");
        return ResponseUtils.createResponseEntity(service.getAll(request));
    }
}
