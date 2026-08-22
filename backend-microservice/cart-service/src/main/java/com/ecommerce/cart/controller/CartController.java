package com.ecommerce.cart.controller;

import com.ecommerce.cart.model.request.CartDetailRequest;
import com.ecommerce.cart.model.request.CartGetAllRequest;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.util.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/v1/permitall/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCart(CartGetAllRequest req, @RequestHeader("X-User-Id") String customerId) {
        req.setIdUser(customerId);
        return ResponseUtils.createResponseEntity(cartService.getAllProductCart(req));
    }

    @PostMapping
    public ResponseEntity<?> createCartDetail(
            @RequestBody CartDetailRequest request,
            @RequestHeader("X-User-Id") String customerId
    ) {
        request.setIdCustomer(customerId);
        return ResponseUtils.createResponseEntity(cartService.createCartDetail(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> deleteCartDetail(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String customerId
    ) {
        return ResponseUtils.createResponseEntity(cartService.deleteCartDetail(id, customerId));
    }
}
