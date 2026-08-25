package com.ecommerce.common.catalog;

import java.math.BigDecimal;
import java.util.List;

public record CatalogVariantSnapshot(
        String id,
        String productId,
        String categoryId,
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
    public CatalogVariantSnapshot(String id, String productId, String sellerId, String sku, String productName,
                                  String variantLabel, List<VariantSelection> selections, BigDecimal salePrice,
                                  Integer quantity, String imageUrl, String status) {
        this(id, productId, null, sellerId, sku, productName, variantLabel, selections, salePrice, quantity, imageUrl, status);
    }

    public record VariantSelection(
            String axisId,
            String axisName,
            String valueId,
            String value
    ) {}
}
