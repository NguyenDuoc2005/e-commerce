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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/san-pham-chi-tiet")
@CrossOrigin(origins = "*")
public class AdminProductDetailController {

    private final ProductDetailService service;

    public AdminProductDetailController(ProductDetailService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAll(ProductDetailSearchRequest request) {
        return ResponseUtils.createResponseEntity(service.getAll(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.changeSanPhamStatus(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.getSPCTById(id));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.getDetailSPCT(id));
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
    public ResponseEntity<?> modify(@ModelAttribute ProductDetailRequest request) {
        return ResponseUtils.createResponseEntity(service.modifySanPham(request));
    }

    @PostMapping("update")
    public ResponseEntity<?> update(@ModelAttribute ProductDetailRequest request) {
        return ResponseUtils.createResponseEntity(service.updateSanPham(request));
    }

    @GetMapping("/list-sp")
    public ResponseEntity<?> getListSP() {
        return ResponseUtils.createResponseEntity(service.getListThemSanPham());
    }
}
