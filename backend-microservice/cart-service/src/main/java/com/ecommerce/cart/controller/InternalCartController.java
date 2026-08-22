package com.ecommerce.cart.controller;

import com.ecommerce.cart.repository.CartDetailRepository;
import com.ecommerce.cart.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/carts")
public class InternalCartController {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;

    public InternalCartController(CartRepository cartRepository, CartDetailRepository cartDetailRepository) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
    }

    @DeleteMapping("/items")
    @Transactional
    public void deleteItems(@RequestParam String customerId, @RequestParam List<String> productDetailIds) {
        if (productDetailIds == null || productDetailIds.isEmpty()) {
            return;
        }
        cartRepository.findByCustomerId(customerId).ifPresent(cart ->
                productDetailIds.forEach(productDetailId ->
                        cartDetailRepository.deleteByCartIdAndProductDetailId(cart.getId(), productDetailId)));
    }
}
