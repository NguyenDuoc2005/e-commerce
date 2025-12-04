package com.be.server.core.admin.nhanvien.controller;

import com.be.server.core.admin.nhanvien.model.request.ADNhanVienRequest;
import com.be.server.core.admin.nhanvien.model.request.ADNhanVienSearchRequest;
import com.be.server.core.admin.nhanvien.model.request.CheckDuplicateRequest;
import com.be.server.core.admin.nhanvien.model.request.CheckDuplicateResponse;
import com.be.server.core.admin.nhanvien.service.ADNhanVienService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_NHAN_VIEN)
@Slf4j
@CrossOrigin(origins = "*")
public class ADNhanVienController {

    public final ADNhanVienService  service;

    @GetMapping
    public ResponseEntity<?> getALl(ADNhanVienSearchRequest request) {
        return Helper.createResponseEntity(service.getAllNhanVien(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getNhanVienById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADNhanVienRequest request) {

        return Helper.createResponseEntity(service.modifyMNhanVien(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeNhanVienStatus(id));
    }

    @PutMapping("/{id}/change-role")
    public ResponseEntity<?> changeRole(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeNhanVienRole(id));
    }


    @PostMapping("/check-duplicate")
    public ResponseEntity checkDuplicate(@RequestBody CheckDuplicateRequest request) {
        boolean exists = service.checkDuplicateField(
                request.getField(),
                request.getValue(),
                request.getExcludeId()
        );
        return ResponseEntity.ok(new CheckDuplicateResponse(exists));
    }
}


