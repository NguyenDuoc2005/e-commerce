package com.ecommerce.catalog.service;

import com.ecommerce.catalog.model.request.ProductAggregateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductAggregateValidatorTest {

    private final ProductAggregateValidator validator = new ProductAggregateValidator();

    @Test
    void acceptsExactlyOneDefaultVariantWhenProductHasNoAxis() {
        ProductAggregateRequest request = request();
        request.setVariants(List.of(variant("SKU-DEFAULT", true, List.of())));

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void acceptsVariantMappedOnceToEachOfTwoAxes() {
        ProductAggregateRequest request = request();
        request.setVariantAxes(List.of(
                axis("color", "Mau", 1, value("red", "Do")),
                axis("size", "Size", 2, value("size-40", "40"))
        ));
        request.setVariants(List.of(variant("SKU-RED-40", false, List.of("red", "size-40"))));

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void rejectsMoreThanTwoAxes() {
        ProductAggregateRequest request = request();
        request.setVariantAxes(List.of(
                axis("a1", "Axis 1", 1, value("v1", "Value 1")),
                axis("a2", "Axis 2", 2, value("v2", "Value 2")),
                axis("a3", "Axis 3", 3, value("v3", "Value 3"))
        ));
        request.setVariants(List.of(variant("SKU-1", false, List.of("v1", "v2", "v3"))));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );

        assertEquals("VARIANT_AXIS_LIMIT_EXCEEDED", error.getMessage());
    }

    @Test
    void rejectsDuplicateVariantCombination() {
        ProductAggregateRequest request = request();
        request.setVariantAxes(List.of(axis("color", "Mau", 1, value("red", "Do"))));
        request.setVariants(List.of(
                variant("SKU-RED-1", false, List.of("red")),
                variant("SKU-RED-2", false, List.of("red"))
        ));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );

        assertEquals("DUPLICATE_VARIANT_COMBINATION", error.getMessage());
    }

    @Test
    void rejectsNegativePriceAndStockBeforePersistence() {
        ProductAggregateRequest negativePrice = request();
        ProductAggregateRequest.VariantInput priceVariant = variant("SKU-PRICE", true, List.of());
        priceVariant.setSalePrice(BigDecimal.valueOf(-1));
        negativePrice.setVariants(List.of(priceVariant));
        assertEquals("VARIANT_PRICE_INVALID", assertThrows(IllegalArgumentException.class,
                () -> validator.validate(negativePrice)).getMessage());

        ProductAggregateRequest negativeStock = request();
        ProductAggregateRequest.VariantInput stockVariant = variant("SKU-STOCK", true, List.of());
        stockVariant.setQuantity(-1);
        negativeStock.setVariants(List.of(stockVariant));
        assertEquals("VARIANT_QUANTITY_INVALID", assertThrows(IllegalArgumentException.class,
                () -> validator.validate(negativeStock)).getMessage());
    }

    private static ProductAggregateRequest request() {
        ProductAggregateRequest request = new ProductAggregateRequest();
        request.setCategoryId("category-1");
        request.setName("Product");
        return request;
    }

    private static ProductAggregateRequest.AxisInput axis(
            String clientKey,
            String name,
            int displayOrder,
            ProductAggregateRequest.AxisValueInput... values
    ) {
        ProductAggregateRequest.AxisInput axis = new ProductAggregateRequest.AxisInput();
        axis.setClientKey(clientKey);
        axis.setName(name);
        axis.setDisplayOrder(displayOrder);
        axis.setValues(List.of(values));
        return axis;
    }

    private static ProductAggregateRequest.AxisValueInput value(String clientKey, String value) {
        ProductAggregateRequest.AxisValueInput input = new ProductAggregateRequest.AxisValueInput();
        input.setClientKey(clientKey);
        input.setValue(value);
        return input;
    }

    private static ProductAggregateRequest.VariantInput variant(
            String sku,
            boolean defaultVariant,
            List<String> selections
    ) {
        ProductAggregateRequest.VariantInput variant = new ProductAggregateRequest.VariantInput();
        variant.setSku(sku);
        variant.setSalePrice(BigDecimal.TEN);
        variant.setQuantity(1);
        variant.setDefaultVariant(defaultVariant);
        variant.setSelectionValueKeys(selections);
        return variant;
    }
}
