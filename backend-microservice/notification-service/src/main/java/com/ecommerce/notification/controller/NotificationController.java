package com.ecommerce.notification.controller;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.notification.model.EmailRequest;
import com.ecommerce.notification.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailRequest request) {
        emailService.send(request);
        return ResponseEntity.ok(new ResponseObject<>(null, HttpStatus.OK, "Gui email thanh cong"));
    }
}
