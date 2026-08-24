package com.ecommerce.common.catalog;

import java.math.BigDecimal;
import java.util.List;

public record CatalogVariantSnapshot(
        String id,
        String productId,
        String sellerId,
        String sku,
        String productName,
        String variantLabel,
        List<VariantSelection> selections,
        BigDecimal salePrice,
        Integer quantity,
        String imageUrl,
        String status
) {
    public record VariantSelection(
            String axisId,
            String axisName,
            String valueId,
            String value
    ) {}
}
