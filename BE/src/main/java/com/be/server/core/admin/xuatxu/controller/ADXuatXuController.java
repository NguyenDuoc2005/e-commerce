package com.be.server.core.admin.xuatxu.controller;

import com.be.server.core.admin.xuatxu.model.request.ADXuatXuRequest;
import com.be.server.core.admin.xuatxu.model.request.ADXuatXuSearchRequest;
import com.be.server.core.admin.xuatxu.service.ADXuatXuService;
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
@RequestMapping(MappingConstants.API_ADMIN_XUAT_XU)
@Slf4j
@CrossOrigin(origins = "*")
public class ADXuatXuController {

    public final ADXuatXuService service;

    @GetMapping
    public ResponseEntity<?> test(ADXuatXuSearchRequest request) {
        return Helper.createResponseEntity(service.getAllXuatXu(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getXuatXuById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADXuatXuRequest request) {

        return Helper.createResponseEntity(service.modifyXuatXu(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeXuatXuStatus(id));
    }
}

