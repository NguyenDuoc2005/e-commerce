package com.be.server.core.admin.mausac.controller;

import com.be.server.core.admin.mausac.model.request.ADMauSacRequest;
import com.be.server.core.admin.mausac.model.request.ADMauSacSearchRequest;
import com.be.server.core.admin.mausac.service.ADMauSacService;
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
@RequestMapping(MappingConstants.API_ADMIN_MAU_SAC)
@Slf4j
@CrossOrigin(origins = "*")
public class ADMauSacController {

    public final ADMauSacService service;

    @GetMapping
    public ResponseEntity<?> test(ADMauSacSearchRequest request) {
        return Helper.createResponseEntity(service.getAllMauSac(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getMauSacById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADMauSacRequest request) {

        return Helper.createResponseEntity(service.modifyMauSac(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeMauSacStatus(id));
    }
}

