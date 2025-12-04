package com.be.server.core.admin.khachhang.controller;

import com.be.server.core.admin.khachhang.model.request.ADKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.ADKhachHangSearchRequest;
import com.be.server.core.admin.khachhang.service.ADKhachHangService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_KHACH_HANG)
@Slf4j
@CrossOrigin(origins = "*")
public class ADKhachHangController {

    public final ADKhachHangService service;

    @GetMapping
    public ResponseEntity<?> test(ADKhachHangSearchRequest request) {
        return Helper.createResponseEntity(service.getAllKhachHang(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getKhachHangById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADKhachHangRequest request) {

        return Helper.createResponseEntity(service.modifyKhachHang(request));
    }

    @PutMapping
    public ResponseEntity<?> updateKhachHang(@ModelAttribute ADKhachHangRequest request) {

        return Helper.createResponseEntity(service.updateKhachHang(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeKhachHangStatus(id));
    }
}

