package com.be.server.core.permitall.giohang.controller;


import com.be.server.core.permitall.giohang.model.request.CartDetailRequest;
import com.be.server.core.permitall.giohang.model.request.CartGetAllRequest;
import com.be.server.core.permitall.giohang.service.CartService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_PERMITALL_CART)
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getAllCart (CartGetAllRequest req ){
        return Helper.createResponseEntity(cartService.getAllProductCart(req));
    }

    @PostMapping
    public ResponseEntity<?> createCartDetail (@RequestBody  CartDetailRequest request ){
        return Helper.createResponseEntity(cartService.createCartDetail(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> createCartDetail (@PathVariable("id") String id ){
        return Helper.createResponseEntity(cartService.deleteCartDetail(id));
    }








}
