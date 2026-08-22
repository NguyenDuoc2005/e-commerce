package com.ecommerce.catalog.model.response;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.AttributeNormalizationStatus;

import java.util.List;

public record AdminAttributeResponse(
        String id,
        String code,
        String name,
        AttributeDataType dataType,
        AttributeNormalizationStatus normalizationStatus,
        String creatorSellerId,
        String mergedIntoAttributeId,
        long usageCount,
        List<DynamicAttributeOptionResponse> options,
        List<CategoryRef> categories
) {
    public record CategoryRef(String id, String name, boolean filterable) {
    }
}
