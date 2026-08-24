package com.ecommerce.order.client;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogClientContractTest {
    @Test
    void usesCanonicalTypedVariantRoutesWithoutShoeSpecificSearchArguments() throws Exception {
        assertEquals(CatalogVariantSnapshot.class,
                CatalogClient.class.getMethod("getProductVariant", String.class).getReturnType());
        assertEquals("/product-variants/{id}", CatalogClient.class.getMethod("getProductVariant", String.class)
                .getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/product-variants/search", CatalogClient.class.getMethod("searchProductVariants",
                        String.class, String.class, String.class, BigDecimal.class, BigDecimal.class)
                .getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/product-variants/{id}/stock/adjust", CatalogClient.class
                .getMethod("adjustStock", String.class, int.class).getAnnotation(PostMapping.class).value()[0]);
        assertThrows(NoSuchMethodException.class, () -> CatalogClient.class.getMethod("searchProductDetails",
                String.class, String.class, String.class, String.class, String.class, String.class,
                String.class, String.class, String.class, Double.class, Double.class));
    }
}
