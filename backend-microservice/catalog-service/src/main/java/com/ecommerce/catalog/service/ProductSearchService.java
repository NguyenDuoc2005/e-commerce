package com.ecommerce.catalog.service;

import com.ecommerce.common.base.ResponseObject;

public interface ProductSearchService {
    ResponseObject<?> search(String keyword, String category, Double minPrice, Double maxPrice);
}
