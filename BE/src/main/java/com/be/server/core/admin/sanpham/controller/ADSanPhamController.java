package com.be.server.core.admin.sanpham.controller;

import com.be.server.core.admin.sanpham.model.request.ADSanPhamRequest;
import com.be.server.core.admin.sanpham.model.request.ADSanPhamSearchRequest;
import com.be.server.core.admin.sanpham.service.ADSanPhamService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_SAN_PHAM)
@Slf4j
@CrossOrigin(origins = "*")
public class ADSanPhamController {

    private final ADSanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<?> getALl(ADSanPhamSearchRequest request) {

        return Helper.createResponseEntity(sanPhamService.getAll(request));
    }

    @GetMapping("/list-thuong-hieu")
    public ResponseEntity<?> getListThuongHieu() {

        return Helper.createResponseEntity(sanPhamService.getListThuongHieu());

    }

    @GetMapping("/list-xuat-xu")
    public ResponseEntity<?> getListXuatXu() {

        return Helper.createResponseEntity(sanPhamService.getXuatXu());

    }

    @GetMapping("/list-loai-de")
    public ResponseEntity<?> getListLoaiDe() {

        return Helper.createResponseEntity(sanPhamService.getListLoaiDe());

    }

    @GetMapping("/list-danh-muc")
    public ResponseEntity<?> getListDanhMuc() {

        return Helper.createResponseEntity(sanPhamService.getListDanhMuc());

    }

    @GetMapping("/list-chat-lieu")
    public ResponseEntity<?> getListChatLieu() {

        return Helper.createResponseEntity(sanPhamService.getListChetLieu());

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(sanPhamService.getSanPhamById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADSanPhamRequest request) {

        return Helper.createResponseEntity(sanPhamService.modifySanPham(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(sanPhamService.changeSanPhamStatus(id));
    }

}
