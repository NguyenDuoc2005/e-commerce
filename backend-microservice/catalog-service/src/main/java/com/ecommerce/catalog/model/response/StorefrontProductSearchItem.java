package com.ecommerce.catalog.model.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record StorefrontProductSearchItem(
        String id,
        String sellerId,
        String name,
        String status,
        CategorySummary category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        long totalQuantity,
        int activeVariantCount,
        String thumbnailUrl,
        BigDecimal ratingAverage,
        long ratingCount,
        Map<String, List<String>> highlights
) {
    public record CategorySummary(String id, String name, String slug) {}
}