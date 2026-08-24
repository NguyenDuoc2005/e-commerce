package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductAttributeValue;
import com.ecommerce.catalog.entity.ProductImage;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.entity.ProductVariantAxis;
import com.ecommerce.catalog.entity.ProductVariantAxisValue;
import com.ecommerce.catalog.entity.ProductVariantAxisValueMapping;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductSchemaContractTest {

    @Test
    void productAndVariantNoLongerExposeLegacyShoeRelations() {
        assertMissing(Product.class, "brand");
        assertMissing(Product.class, "origin");
        assertMissing(Product.class, "material");
        assertMissing(Product.class, "soleType");
        assertMissing(ProductVariant.class, "color");
        assertMissing(ProductVariant.class, "size");
        assertMissing(ProductVariant.class, "sellerId");
    }

    @Test
    void variantUsesTargetMoneyAndCombinationContract() throws Exception {
        assertEquals(BigDecimal.class, field(ProductVariant.class, "salePrice").getType());
        assertEquals("product_id", joinColumn(ProductVariant.class, "product").name());
        assertNotNull(field(ProductVariant.class, "combinationKey"));
        assertNotNull(field(ProductVariant.class, "defaultVariant"));
    }

    @Test
    void targetEntitiesUseExpectedTablesAndForeignKeys() throws Exception {
        assertTable(Category.class, "category");
        assertTable(ProductImage.class, "product_image");
        assertTable(ProductVariantAxis.class, "product_variant_axis");
        assertTable(ProductVariantAxisValue.class, "product_variant_axis_value");
        assertTable(ProductVariantAxisValueMapping.class, "product_variant_axis_value_mapping");
        assertEquals("attribute_definition_id", joinColumn(ProductAttributeValue.class, "definition").name());
        assertEquals("attribute_option_id", joinColumn(ProductAttributeValue.class, "option").name());
    }

    @Test
    void repositoriesOnlyAddressTargetAggregateProperties() throws Exception {
        assertNotNull(ProductVariantRepository.class.getMethod(
                "existsByProduct_IdAndCombinationKey", String.class, String.class));
        assertNotNull(ProductVariantAxisRepository.class.getMethod(
                "findByProduct_IdOrderByDisplayOrderAsc", String.class));
        assertNotNull(ProductAttributeValueRepository.class.getMethod(
                "findLockedByProductAndDefinition", String.class, String.class));
    }

    @Test
    void targetSchemaHasDatabaseGuardForSingletonAttributeValues() throws Exception {
        String schema = Files.readString(Path.of(
                "src/main/resources/db/migration/manual/p1_product_domain_reset.sql"));
        assertTrue(schema.contains("`value_slot`"));
        assertTrue(schema.contains("`uk_product_attribute_value_slot`"));
    }

    private static void assertTable(Class<?> type, String tableName) {
        Table table = type.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals(tableName, table.name());
    }

    private static JoinColumn joinColumn(Class<?> type, String fieldName) throws Exception {
        JoinColumn annotation = field(type, fieldName).getAnnotation(JoinColumn.class);
        assertNotNull(annotation);
        return annotation;
    }

    private static Field field(Class<?> type, String fieldName) throws Exception {
        return type.getDeclaredField(fieldName);
    }

    private static void assertMissing(Class<?> type, String fieldName) {
        assertThrows(NoSuchFieldException.class, () -> type.getDeclaredField(fieldName));
    }
}
