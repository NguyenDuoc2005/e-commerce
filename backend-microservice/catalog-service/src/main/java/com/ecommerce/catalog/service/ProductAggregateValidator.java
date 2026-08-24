package com.ecommerce.catalog.service;

import com.ecommerce.catalog.model.request.ProductAggregateRequest;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ProductAggregateValidator {

    public void validate(ProductAggregateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PRODUCT_REQUEST_REQUIRED");
        }
        if (request.getCategoryId() == null || request.getCategoryId().isBlank()) {
            throw new IllegalArgumentException("CATEGORY_INVALID");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("PRODUCT_NAME_REQUIRED");
        }
        List<ProductAggregateRequest.AxisInput> axes = safe(request.getVariantAxes());
        List<ProductAggregateRequest.VariantInput> variants = safe(request.getVariants());
        if (axes.size() > 2) {
            throw new IllegalArgumentException("VARIANT_AXIS_LIMIT_EXCEEDED");
        }
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("PRODUCT_REQUIRES_VARIANT");
        }

        Set<Integer> axisOrders = new HashSet<>();
        Set<String> axisNames = new HashSet<>();
        Set<String> allValueKeys = new HashSet<>();
        Map<String, String> valueKeyToAxis = new HashMap<>();
        for (ProductAggregateRequest.AxisInput axis : axes) {
            if (axis.getClientKey() == null || axis.getClientKey().isBlank()
                    || axis.getDisplayOrder() == null || axis.getDisplayOrder() < 1 || axis.getDisplayOrder() > 2
                    || !axisOrders.add(axis.getDisplayOrder()) || !axisNames.add(normalize(axis.getName()))) {
                throw new IllegalArgumentException("DUPLICATE_AXIS_OR_VALUE");
            }
            if (safe(axis.getValues()).isEmpty()) {
                throw new IllegalArgumentException("AXIS_REQUIRES_VALUE");
            }
            Set<String> normalizedValues = new HashSet<>();
            for (ProductAggregateRequest.AxisValueInput value : safe(axis.getValues())) {
                if (value.getClientKey() == null || value.getClientKey().isBlank()
                        || !allValueKeys.add(value.getClientKey())
                        || !normalizedValues.add(normalize(value.getValue()))) {
                    throw new IllegalArgumentException("DUPLICATE_AXIS_OR_VALUE");
                }
                valueKeyToAxis.put(value.getClientKey(), axis.getClientKey());
            }
        }

        Set<String> skus = new HashSet<>();
        Set<String> combinations = new HashSet<>();
        int defaultCount = 0;
        for (ProductAggregateRequest.VariantInput variant : variants) {
            if (variant.getSku() == null || variant.getSku().isBlank()
                    || !skus.add(normalize(variant.getSku()))) {
                throw new IllegalArgumentException("DUPLICATE_SKU");
            }
            if (variant.getSalePrice() == null || variant.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("VARIANT_PRICE_INVALID");
            }
            if (variant.getQuantity() == null || variant.getQuantity() < 0) {
                throw new IllegalArgumentException("VARIANT_QUANTITY_INVALID");
            }
            if (variant.isDefaultVariant()) defaultCount++;
            List<String> selections = safe(variant.getSelectionValueKeys());
            if (axes.isEmpty()) {
                if (!variant.isDefaultVariant() || !selections.isEmpty()) {
                    throw new IllegalArgumentException("DEFAULT_VARIANT_INVALID");
                }
                if (!combinations.add("DEFAULT")) {
                    throw new IllegalArgumentException("DUPLICATE_VARIANT_COMBINATION");
                }
            } else {
                if (variant.isDefaultVariant() || selections.size() != axes.size()) {
                    throw new IllegalArgumentException("VARIANT_MAPPING_INVALID");
                }
                Set<String> selectedAxes = new HashSet<>();
                for (String key : selections) {
                    String axisKey = valueKeyToAxis.get(key);
                    if (axisKey == null || !selectedAxes.add(axisKey)) {
                        throw new IllegalArgumentException("VARIANT_MAPPING_INVALID");
                    }
                }
                String combination = axes.stream()
                        .sorted(java.util.Comparator.comparing(ProductAggregateRequest.AxisInput::getDisplayOrder))
                        .map(axis -> selections.stream()
                                .filter(key -> axis.getClientKey().equals(valueKeyToAxis.get(key)))
                                .findFirst().orElseThrow(() -> new IllegalArgumentException("VARIANT_MAPPING_INVALID")))
                        .reduce((left, right) -> left + "|" + right).orElseThrow();
                if (!combinations.add(combination)) {
                    throw new IllegalArgumentException("DUPLICATE_VARIANT_COMBINATION");
                }
            }
        }
        if ((axes.isEmpty() && (variants.size() != 1 || defaultCount != 1)) || (!axes.isEmpty() && defaultCount != 0)) {
            throw new IllegalArgumentException("DEFAULT_VARIANT_INVALID");
        }
    }

    public static String normalize(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").replace('đ', 'd').replaceAll("\\s+", " ");
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
