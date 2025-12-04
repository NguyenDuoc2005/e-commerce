package com.be.server.core.admin.thuonghieu.controller;

import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuRequest;
import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuSearchRequest;
import com.be.server.core.admin.thuonghieu.service.ADThuongHieuService;
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
@RequestMapping(MappingConstants.API_ADMIN_THUONG_HIEU)
@Slf4j
@CrossOrigin(origins = "*")
public class ADThuongHieuController {

    public final ADThuongHieuService service;

    @GetMapping
    public ResponseEntity<?> test(ADThuongHieuSearchRequest request) {
        return Helper.createResponseEntity(service.getAllThuongHieu(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getThuongHieuById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADThuongHieuRequest request) {

        return Helper.createResponseEntity(service.modifyThuongHieu(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeSizeStatus(id));
    }
}

