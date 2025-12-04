package com.be.server.core.admin.chatlieu.controller;

import com.be.server.core.admin.chatlieu.model.request.ADChatLieuRequest;
import com.be.server.core.admin.chatlieu.model.request.ADChatLieuSearchRequest;
import com.be.server.core.admin.chatlieu.service.ADChatLieuService;
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
@RequestMapping(MappingConstants.API_ADMIN_CHAT_LIEU)
@Slf4j
@CrossOrigin(origins = "*")
public class ADChatLieuController {

    public final ADChatLieuService service;

    @GetMapping
    public ResponseEntity<?> test(ADChatLieuSearchRequest request) {
        return Helper.createResponseEntity(service.getAll(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADChatLieuRequest request) {

        return Helper.createResponseEntity(service.modify(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(service.changeStatus(id));
    }
}

