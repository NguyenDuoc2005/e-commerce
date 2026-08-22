package com.ecommerce.seller.controller;

import com.ecommerce.seller.model.ReviewReplyRequest;
import com.ecommerce.seller.model.ReviewRequest;
import com.ecommerce.seller.security.JwtPrincipalResolver;
import com.ecommerce.seller.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReviewController {

    private final ReviewService service;
    private final JwtPrincipalResolver principalResolver;

    public ReviewController(ReviewService service, JwtPrincipalResolver principalResolver) {
        this.service = service;
        this.principalResolver = principalResolver;
    }

    @PostMapping("/api/v1/permitall/reviews")
    public ResponseEntity<?> create(@Valid @RequestBody ReviewRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(Map.of("data", service.create(principalResolver.customerId(servletRequest), request)));
    }

    @GetMapping("/api/v1/permitall/reviews")
    public ResponseEntity<?> publicReviews(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String sellerId
    ) {
        return ResponseEntity.ok(Map.of("data", service.publicReviews(productId, sellerId)));
    }

    @GetMapping("/api/v1/permitall/reviews/mine")
    public ResponseEntity<?> customerReviews(HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("data", service.customerReviews(principalResolver.customerId(request))));
    }

    @GetMapping("/api/v1/seller/reviews")
    public ResponseEntity<?> sellerReviews(HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("data", service.sellerReviews(principalResolver.sellerId(request))));
    }

    @PutMapping("/api/v1/seller/reviews/{id}/reply")
    public ResponseEntity<?> reply(
            @PathVariable String id,
            @Valid @RequestBody ReviewReplyRequest body,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(Map.of("data", service.reply(principalResolver.sellerId(request), id, body.getReply())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }
}
