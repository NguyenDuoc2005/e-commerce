package com.ecommerce.catalog.model.response;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.AttributeNormalizationStatus;

import java.math.BigDecimal;
import java.util.List;

public record DynamicAttributeResponse(
        String attributeId,
        String name,
        AttributeDataType dataType,
        AttributeNormalizationStatus normalizationStatus,
        boolean ownedBySeller,
        boolean defaultSuggestion,
        boolean filterable,
        boolean required,
        List<DynamicAttributeOptionResponse> options,
        String textValue,
        BigDecimal numberValue,
        String unit,
        List<String> selectedOptionIds,
        Integer displayOrder
) {
}
