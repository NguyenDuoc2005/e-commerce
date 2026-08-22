package com.ecommerce.catalog.model.response;

import com.ecommerce.catalog.constant.AttributeDataType;

import java.math.BigDecimal;
import java.util.List;

public record DynamicFilterResponse(
        String attributeId,
        String name,
        AttributeDataType dataType,
        List<DynamicAttributeOptionResponse> options,
        BigDecimal min,
        BigDecimal max,
        Integer displayOrder
) {
}
