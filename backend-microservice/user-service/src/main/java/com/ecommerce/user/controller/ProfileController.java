package com.ecommerce.user.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.user.model.request.UserUpsertRequest;
import com.ecommerce.user.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permitall/profile")
public class ProfileController {

    private final CustomerService customerService;

    public ProfileController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(customerService.getCustomerById(id));
    }

    @GetMapping("/hd/{id}")
    public ResponseEntity<?> getOrderHistory(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(customerService.getLSKH(id));
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute UserUpsertRequest request) {
        return ResponseUtils.createResponseEntity(customerService.modifyCustomer(request));
    }
}
