package com.ecommerce.user.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.user.model.request.ADStaffSearchRequest;
import com.ecommerce.user.model.request.CheckDuplicateRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;
import com.ecommerce.user.model.response.CheckDuplicateResponse;
import com.ecommerce.user.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/nhan-vien")
@CrossOrigin(origins = "*")
public class AdminEmployeeController {

    private final EmployeeService service;

    public AdminEmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAll(ADStaffSearchRequest request) {
        return ResponseUtils.createResponseEntity(service.getAllStaff(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.getStaffById(id));
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute UserUpsertRequest request) {
        return ResponseUtils.createResponseEntity(service.modifyStaff(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.changeStaffStatus(id));
    }

    @PutMapping("/{id}/change-role")
    public ResponseEntity<?> changeRole(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(service.changeStaffRole(id));
    }

    @PostMapping("/check-duplicate")
    public ResponseEntity<CheckDuplicateResponse> checkDuplicate(@RequestBody CheckDuplicateRequest request) {
        boolean exists = service.checkDuplicateField(request.getField(), request.getValue(), request.getExcludeId());
        return ResponseEntity.ok(new CheckDuplicateResponse(exists));
    }
}
