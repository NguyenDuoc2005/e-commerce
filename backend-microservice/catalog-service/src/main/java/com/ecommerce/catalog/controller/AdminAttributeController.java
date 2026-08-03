package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.request.AttributeRequest;
import com.ecommerce.catalog.model.request.AttributeSearchRequest;
import com.ecommerce.catalog.service.AttributeService;
import com.ecommerce.common.util.ResponseUtils;
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
@CrossOrigin(origins = "*")
public class AdminAttributeController {

    private final AttributeService attributeService;

    public AdminAttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @GetMapping({
            "/api/v1/admin/mau-sac",
            "/api/v1/admin/size",
            "/api/v1/admin/thuong-hieu",
            "/api/v1/admin/xuat-xu",
            "/api/v1/admin/chat-lieu",
            "/api/v1/admin/danh-muc",
            "/api/v1/admin/loai-de"
    })
    public ResponseEntity<?> getAll(AttributeSearchRequest request, jakarta.servlet.http.HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(attributeService.getAll(key(servletRequest), request));
    }

    @GetMapping({
            "/api/v1/admin/mau-sac/{id}",
            "/api/v1/admin/size/{id}",
            "/api/v1/admin/thuong-hieu/{id}",
            "/api/v1/admin/xuat-xu/{id}",
            "/api/v1/admin/chat-lieu/{id}",
            "/api/v1/admin/danh-muc/{id}",
            "/api/v1/admin/loai-de/{id}"
    })
    public ResponseEntity<?> getById(@PathVariable String id, jakarta.servlet.http.HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(attributeService.getById(key(servletRequest), id));
    }

    @PostMapping({
            "/api/v1/admin/mau-sac",
            "/api/v1/admin/size",
            "/api/v1/admin/thuong-hieu",
            "/api/v1/admin/xuat-xu",
            "/api/v1/admin/chat-lieu",
            "/api/v1/admin/danh-muc",
            "/api/v1/admin/loai-de"
    })
    public ResponseEntity<?> modify(@ModelAttribute AttributeRequest request, jakarta.servlet.http.HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(attributeService.modify(key(servletRequest), request));
    }

    @PutMapping({
            "/api/v1/admin/mau-sac/{id}/change-status",
            "/api/v1/admin/size/{id}/change-status",
            "/api/v1/admin/thuong-hieu/{id}/change-status",
            "/api/v1/admin/xuat-xu/{id}/change-status",
            "/api/v1/admin/chat-lieu/{id}/change-status",
            "/api/v1/admin/danh-muc/{id}/change-status",
            "/api/v1/admin/loai-de/{id}/change-status"
    })
    public ResponseEntity<?> changeStatus(@PathVariable String id, jakarta.servlet.http.HttpServletRequest servletRequest) {
        return ResponseUtils.createResponseEntity(attributeService.changeStatus(key(servletRequest), id));
    }

    private String key(jakarta.servlet.http.HttpServletRequest request) {
        String[] parts = request.getRequestURI().split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("admin".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        throw new IllegalArgumentException("Cannot resolve catalog attribute route: " + request.getRequestURI());
    }
}
