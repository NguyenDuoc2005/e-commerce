package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.service.ProductService;
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
@RequestMapping("/api/v1/admin/san-pham")
@CrossOrigin(origins = "*")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getAll(request));
    }

    @GetMapping("/list-thuong-hieu")
    public ResponseEntity<?> getListThuongHieu() {
        return ResponseUtils.createResponseEntity(productService.getListThuongHieu());
    }

    @GetMapping("/list-xuat-xu")
    public ResponseEntity<?> getListXuatXu() {
        return ResponseUtils.createResponseEntity(productService.getXuatXu());
    }

    @GetMapping("/list-loai-de")
    public ResponseEntity<?> getListLoaiDe() {
        return ResponseUtils.createResponseEntity(productService.getListLoaiDe());
    }

    @GetMapping("/list-danh-muc")
    public ResponseEntity<?> getListDanhMuc() {
        return ResponseUtils.createResponseEntity(productService.getListDanhMuc());
    }

    @GetMapping("/list-chat-lieu")
    public ResponseEntity<?> getListChatLieu() {
        return ResponseUtils.createResponseEntity(productService.getListChatLieu());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(productService.getSanPhamById(id));
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute ProductRequest request) {
        return ResponseUtils.createResponseEntity(productService.modifySanPham(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(productService.changeSanPhamStatus(id));
    }
}
