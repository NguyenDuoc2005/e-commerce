package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.util.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permitall/san-pham")
public class PublicProductController {

    private final ProductService productService;

    public PublicProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/list-thuong-hieu")
    public ResponseEntity<?> getListThuongHieu() {
        return ResponseUtils.createResponseEntity(productService.getListThuongHieu());
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

    @GetMapping("/get-all/danh-sach-san-pham")
    public ResponseEntity<?> getDanhSachSanPham(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getAll(request));
    }

    @GetMapping("/get-all/san-pham-moi")
    public ResponseEntity<?> getSanPhamMoi(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getSanPhamMoi(request));
    }

    @GetMapping("/get-all/san-pham-giam-gia")
    public ResponseEntity<?> getSanPhamGiamGia(ProductSearchRequest request) {
        return ResponseUtils.createResponseEntity(productService.getSanPhamGiamGia(request));
    }
}
