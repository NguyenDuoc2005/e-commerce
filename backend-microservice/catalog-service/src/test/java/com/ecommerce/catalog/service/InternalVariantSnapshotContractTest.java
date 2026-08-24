package com.ecommerce.catalog.service;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InternalVariantSnapshotContractTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesCanonicalSnapshotsForZeroOneAndTwoAxes() throws Exception {
        assertSnapshot(List.of(), "Mặc định");
        assertSnapshot(List.of(selection("axis-1", "Màu", "value-1", "Đỏ")), "Màu: Đỏ");
        assertSnapshot(List.of(
                selection("axis-1", "Màu", "value-1", "Đỏ"),
                selection("axis-2", "Kích cỡ", "value-2", "40")), "Màu: Đỏ · Kích cỡ: 40");
    }

    private void assertSnapshot(List<CatalogVariantSnapshot.VariantSelection> selections, String label) throws Exception {
        JsonNode json = objectMapper.valueToTree(new CatalogVariantSnapshot(
                "variant", "product", "seller", "SKU", "Product", label, selections,
                BigDecimal.TEN, 5, "image", "ACTIVE"));
        assertEquals(selections.size(), json.get("selections").size());
        assertEquals(label, json.get("variantLabel").asText());
        assertEquals(0, BigDecimal.TEN.compareTo(json.get("salePrice").decimalValue()));
        assertFalse(json.has("color"));
        assertFalse(json.has("size"));
    }

    private static CatalogVariantSnapshot.VariantSelection selection(
            String axisId, String axisName, String valueId, String value) {
        return new CatalogVariantSnapshot.VariantSelection(axisId, axisName, valueId, value);
    }
}
