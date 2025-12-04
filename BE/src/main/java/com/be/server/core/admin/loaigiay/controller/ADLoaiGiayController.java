package com.be.server.core.admin.loaigiay.controller;

import com.be.server.core.admin.loaigiay.model.request.ADLoaiGiayRequest;
import com.be.server.core.admin.loaigiay.model.request.ADLoaiGiaySearchRequest;
import com.be.server.core.admin.loaigiay.service.ADLoaiGiayService;
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
@RequestMapping(MappingConstants.API_ADMIN_DANH_MUC)
@Slf4j
@CrossOrigin(origins = "*")
public class ADLoaiGiayController {

    public final ADLoaiGiayService service;

    @GetMapping
    public ResponseEntity<?> test(ADLoaiGiaySearchRequest request) {
        return Helper.createResponseEntity(service.getAll(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADLoaiGiayRequest request) {

        return Helper.createResponseEntity(service.modify(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeStatus(id));
    }
}

