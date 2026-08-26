package com.ecommerce.seller.controller;

import com.ecommerce.seller.model.SendChatMessageRequest;
import com.ecommerce.seller.model.StartChatRequest;
import com.ecommerce.seller.security.JwtPrincipalResolver;
import com.ecommerce.seller.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatController {
    private final ChatService service;
    private final JwtPrincipalResolver principalResolver;

    public ChatController(ChatService service, JwtPrincipalResolver principalResolver) {
        this.service = service;
        this.principalResolver = principalResolver;
    }

    @PostMapping("/api/v1/buyer/chat/conversations")
    public ResponseEntity<?> start(@Valid @RequestBody StartChatRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.startBuyerConversation(principalResolver.customerId(request), body.getSellerId()));
    }
    @GetMapping("/api/v1/buyer/chat/conversations")
    public ResponseEntity<?> buyerConversations(HttpServletRequest request) {
        return ResponseEntity.ok(service.buyerConversations(principalResolver.customerId(request)));
    }
    @GetMapping("/api/v1/buyer/chat/conversations/{id}/messages")
    public ResponseEntity<?> buyerMessages(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.buyerMessages(id, principalResolver.customerId(request)));
    }
    @PostMapping("/api/v1/buyer/chat/conversations/{id}/messages")
    public ResponseEntity<?> buyerSend(@PathVariable String id, @Valid @RequestBody SendChatMessageRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.sendBuyerMessage(id, principalResolver.customerId(request), body.getContent()));
    }
    @PostMapping("/api/v1/buyer/chat/conversations/{id}/read")
    public ResponseEntity<?> buyerRead(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.markBuyerRead(id, principalResolver.customerId(request)));
    }

    @GetMapping("/api/v1/seller/chat/conversations")
    public ResponseEntity<?> sellerConversations(HttpServletRequest request) {
        return ResponseEntity.ok(service.sellerConversations(principalResolver.sellerId(request)));
    }
    @GetMapping("/api/v1/seller/chat/conversations/{id}/messages")
    public ResponseEntity<?> sellerMessages(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.sellerMessages(id, principalResolver.sellerId(request)));
    }
    @PostMapping("/api/v1/seller/chat/conversations/{id}/messages")
    public ResponseEntity<?> sellerSend(@PathVariable String id, @Valid @RequestBody SendChatMessageRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(service.sendSellerMessage(id, principalResolver.sellerId(request), body.getContent()));
    }
    @PostMapping("/api/v1/seller/chat/conversations/{id}/read")
    public ResponseEntity<?> sellerRead(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(service.markSellerRead(id, principalResolver.sellerId(request)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(RuntimeException error) {
        return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
    }
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> forbidden(SecurityException error) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", error.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation() {
        return ResponseEntity.badRequest().body(Map.of("message", "Tin nhan khong hop le"));
    }
}
