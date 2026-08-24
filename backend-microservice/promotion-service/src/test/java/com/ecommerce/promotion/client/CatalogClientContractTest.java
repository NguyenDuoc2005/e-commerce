package com.ecommerce.promotion.client;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogClientContractTest {
    @Test
    void usesTypedVariantContractAndHasNoLegacyColorOrSizeEndpoints() throws Exception {
        assertEquals(CatalogVariantSnapshot.class,
                CatalogClient.class.getMethod("getProductVariant", String.class).getReturnType());
        assertEquals("/product-variants/{id}", CatalogClient.class.getMethod("getProductVariant", String.class)
                .getAnnotation(GetMapping.class).value()[0]);
        assertThrows(NoSuchMethodException.class, () -> CatalogClient.class.getMethod("getColors"));
        assertThrows(NoSuchMethodException.class, () -> CatalogClient.class.getMethod("getSizes"));
    }
}
