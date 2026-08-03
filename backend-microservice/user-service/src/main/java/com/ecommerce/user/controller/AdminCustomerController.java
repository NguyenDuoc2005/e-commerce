package com.ecommerce.user.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.user.model.request.ADKhachHangSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;
import com.ecommerce.user.service.CustomerService;
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
@RequestMapping("/api/v1/admin/khach-hang")
@CrossOrigin(origins = "*")
public class AdminCustomerController {

    private final CustomerService service;

    public AdminCustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAll(ADKhachHangSearchRequest request) {
        return ResponseUtils.createResponseEntity(service.getAllKhachHang(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.getKhachHangById(id));
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute UserUpsertRequest request) {
        return ResponseUtils.createResponseEntity(service.modifyKhachHang(request));
    }

    @PutMapping
    public ResponseEntity<?> update(@ModelAttribute UserUpsertRequest request) {
        return ResponseUtils.createResponseEntity(service.updateKhachHang(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.changeKhachHangStatus(id));
    }
}
