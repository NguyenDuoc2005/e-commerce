package com.be.server.core.admin.loaide.controller;

import com.be.server.core.admin.loaide.model.request.ADLoaiDeRequest;
import com.be.server.core.admin.loaide.model.request.ADLoaiDeSearchRequest;
import com.be.server.core.admin.loaide.service.ADLoaiDeService;
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
@RequestMapping(MappingConstants.API_ADMIN_LOAI_DE)
@Slf4j
@CrossOrigin(origins = "*")
public class ADLoaiDeController {

    public final ADLoaiDeService service;

    @GetMapping
    public ResponseEntity<?> test(ADLoaiDeSearchRequest request) {
        return Helper.createResponseEntity(service.getAll(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADLoaiDeRequest request) {

        return Helper.createResponseEntity(service.modify(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeStatus(id));
    }
}

