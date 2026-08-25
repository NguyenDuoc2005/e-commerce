package com.ecommerce.seller.controller;

import com.ecommerce.seller.model.CreateReportRequest;
import com.ecommerce.seller.model.ResolveReportRequest;
import com.ecommerce.seller.security.JwtPrincipalResolver;
import com.ecommerce.seller.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class ReportController {
    private final ReportService service;
    private final JwtPrincipalResolver principalResolver;
    public ReportController(ReportService service, JwtPrincipalResolver principalResolver) { this.service = service; this.principalResolver = principalResolver; }

    @PostMapping("/api/v1/buyer/reports")
    public ResponseEntity<?> buyer(@Valid @RequestBody CreateReportRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.create(principalResolver.customerId(request), "BUYER", body));
    }
    @PostMapping("/api/v1/seller/reports")
    public ResponseEntity<?> seller(@Valid @RequestBody CreateReportRequest body, HttpServletRequest request) {
        principalResolver.sellerId(request);
        return ResponseEntity.ok(service.create(principalResolver.customerId(request), "SELLER", body));
    }
    @GetMapping("/api/v1/admin/reports")
    public ResponseEntity<?> list(@RequestParam(required = false) String status, @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(service.adminList(status, targetType, dateFrom, dateTo));
    }
    @GetMapping("/api/v1/admin/reports/{id}") public ResponseEntity<?> detail(@PathVariable String id) { return ResponseEntity.ok(service.adminDetail(id)); }
    @PostMapping("/api/v1/admin/reports/{id}/review") public ResponseEntity<?> review(@PathVariable String id, HttpServletRequest request) { return ResponseEntity.ok(service.review(id, principalResolver.staffId(request))); }
    @PostMapping("/api/v1/admin/reports/{id}/resolve") public ResponseEntity<?> resolve(@PathVariable String id, @Valid @RequestBody ResolveReportRequest body, HttpServletRequest request) { return ResponseEntity.ok(service.resolve(id, principalResolver.staffId(request), body)); }

    @ExceptionHandler({IllegalArgumentException.class, SecurityException.class})
    public ResponseEntity<?> badRequest(RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException e) { return ResponseEntity.badRequest().body(Map.of("message", "Du lieu bao cao khong hop le")); }
}
