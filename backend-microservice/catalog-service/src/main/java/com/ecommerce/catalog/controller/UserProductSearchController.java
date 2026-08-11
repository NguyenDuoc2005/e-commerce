package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.service.ProductSearchService;
import com.ecommerce.common.util.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/user/products")
public class UserProductSearchController {

    private final ProductSearchService productSearchService;

    public UserProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return ResponseUtils.createResponseEntity(productSearchService.search(keyword, category, minPrice, maxPrice));
    }
}
