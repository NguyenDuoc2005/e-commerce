package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.ResolveDisputeRequest;
import com.ecommerce.order.model.request.DisputeMessageRequest;
import com.ecommerce.order.service.DisputeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/disputes")
public class AdminDisputeController {
    private final DisputeService service;
    public AdminDisputeController(DisputeService service) { this.service = service; }
    @GetMapping public ResponseEntity<?> list(@RequestParam(required = false) String status,
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(service.adminList(status, sellerId, dateFrom, dateTo));
    }
    @GetMapping("/{id}") public ResponseEntity<?> detail(@PathVariable String id) { return ResponseEntity.ok(service.adminDetail(id)); }
    @PostMapping("/{id}/take-review") public ResponseEntity<?> takeReview(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.takeReview(staffId(request), id));
    }
    @PostMapping("/{id}/messages") public ResponseEntity<?> message(@PathVariable String id, @Valid @RequestBody DisputeMessageRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.adminMessage(staffId(request), id, body));
    }
    @PostMapping("/{id}/resolve") public ResponseEntity<?> resolve(@PathVariable String id, @Valid @RequestBody ResolveDisputeRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.resolve(staffId(request), id, body));
    }
    @PostMapping("/{id}/close") public ResponseEntity<?> close(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.close(staffId(request), id));
    }
    private String staffId(HttpServletRequest request) {
        String id = request.getHeader("X-User-Id");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Missing staff context");
        return id;
    }
}
