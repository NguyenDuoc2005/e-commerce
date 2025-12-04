package com.be.server.core.permitall.profile.controller;

import com.be.server.core.admin.khachhang.model.request.ADKhachHangRequest;
import com.be.server.core.admin.khachhang.service.ADKhachHangService;
import com.be.server.core.permitall.profile.service.PMProfileService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_PERMITALL_PROFILE)
public class ProfileController {

    public final ADKhachHangService service;

    public final PMProfileService pmProfileService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getKhachHangById(id));
    }

    @GetMapping("/hd/{id}")
    public ResponseEntity<?> getProductBy(@PathVariable String id) {

        return Helper.createResponseEntity(pmProfileService.getLSKH(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADKhachHangRequest request) {

        return Helper.createResponseEntity(service.modifyKhachHang(request));
    }
}
