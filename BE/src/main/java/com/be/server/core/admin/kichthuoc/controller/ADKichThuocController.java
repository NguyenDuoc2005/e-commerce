package com.be.server.core.admin.kichthuoc.controller;

import com.be.server.core.admin.kichthuoc.model.request.ADKichThuocRequest;
import com.be.server.core.admin.kichthuoc.model.request.ADKichThuocSearchRequest;
import com.be.server.core.admin.kichthuoc.service.ADKichThuocService;
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
@RequestMapping(MappingConstants.API_ADMIN_SIZE)
@Slf4j
@CrossOrigin(origins = "*")
public class ADKichThuocController {

    public final ADKichThuocService service;

    @GetMapping
    public ResponseEntity<?> test(ADKichThuocSearchRequest request) {
        return Helper.createResponseEntity(service.getAllSize(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getSizeById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADKichThuocRequest request) {

        return Helper.createResponseEntity(service.modifySize(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeSizeStatus(id));
    }
}

