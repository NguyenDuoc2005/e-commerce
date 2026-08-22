package com.ecommerce.seller.controller;

import com.ecommerce.seller.service.SellerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/sellers")
public class InternalSellerController {

    private final SellerService sellerService;

    public InternalSellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/approved/by-owner")
    public Map<String, Object> approvedByOwner(@RequestParam String ownerCustomerId) {
        return sellerService.approvedByOwner(ownerCustomerId);
    }

    @GetMapping("/{id}/public")
    public Map<String, Object> publicProfile(@PathVariable String id) {
        return sellerService.publicProfile(id);
    }

    @GetMapping("/{id}/owner")
    public Map<String, Object> ownerReference(@PathVariable String id) {
        return sellerService.ownerReference(id);
    }
}
