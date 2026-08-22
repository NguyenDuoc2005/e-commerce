package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.AttributeNormalizationStatus;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import com.ecommerce.catalog.entity.ProductAttributeValue;
import com.ecommerce.catalog.repository.CategoryAttributeSuggestionRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductAttributeDefinitionRepository;
import com.ecommerce.catalog.repository.ProductAttributeOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicAttributeServiceTest {

    @Mock private ProductAttributeDefinitionRepository definitionRepository;
    @Mock private ProductAttributeOptionRepository optionRepository;
    @Mock private CategoryAttributeSuggestionRepository suggestionRepository;
    @Mock private ProductAttributeValueRepository valueRepository;
    @Mock private ProductAttributeValueOptionRepository valueOptionRepository;
    @Mock private CategoryRepository categoryRepository;

    private DynamicAttributeService service;

    @BeforeEach
    void setUp() {
        service = new DynamicAttributeService(
                definitionRepository,
                optionRepository,
                suggestionRepository,
                valueRepository,
                valueOptionRepository,
                categoryRepository,
                new ObjectMapper()
        );
    }

    @Test
    void pendingAttributesAreOnlySuggestedToTheCreatingSeller() {
        Category category = category("CAT-1");
        CategoryAttributeSuggestion ownPending = suggestion(category, definition("A1", "Own", "SELLER-A", AttributeNormalizationStatus.PENDING));
        CategoryAttributeSuggestion otherPending = suggestion(category, definition("A2", "Other", "SELLER-B", AttributeNormalizationStatus.PENDING));
        CategoryAttributeSuggestion standardized = suggestion(category, definition("A3", "Shared", null, AttributeNormalizationStatus.STANDARDIZED));

        when(suggestionRepository.findByCategory_IdAndStatusOrderByDisplayOrderAsc("CAT-1", EntityStatus.ACTIVE))
                .thenReturn(List.of(ownPending, otherPending, standardized));
        when(optionRepository.findByAttribute_IdAndStatusOrderByDisplayOrderAsc(anyString(), any()))
                .thenReturn(List.of());

        var result = service.suggestions("CAT-1", "SELLER-A", null);

        assertEquals(List.of("Own", "Shared"), result.stream().map(item -> item.name()).toList());
    }

    @Test
    void rejectsMoreThanFiftyAttributesPerProduct() throws Exception {
        Product product = product();
        var attributes = java.util.stream.IntStream.range(0, 51)
                .mapToObj(index -> java.util.Map.of(
                        "name", "Attribute " + index,
                        "dataType", "TEXT",
                        "textValue", "value"
                ))
                .toList();
        String json = new ObjectMapper().writeValueAsString(attributes);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.replaceProductAttributes(product, "SELLER-A", json)
        );

        assertEquals("Moi san pham chi duoc toi da 50 thuoc tinh", error.getMessage());
    }

    @Test
    void sellerCanCreateNumberAttribute() {
        Product product = product();
        prepareCustomAttributeSaves();
        String json = """
                [{"name":"Trong luong","dataType":"NUMBER","numberValue":1.25,"unit":"kg"}]
                """;

        service.replaceProductAttributes(product, "SELLER-A", json);

        verify(valueRepository).save(org.mockito.ArgumentMatchers.argThat(value ->
                value.getNumberValue().compareTo(new BigDecimal("1.25")) == 0
                        && "kg".equals(value.getUnit())
                        && value.getAttribute().getDataType() == AttributeDataType.NUMBER
        ));
    }

    @Test
    void sellerCanCreateSingleSelectAttributeAndOptions() {
        Product product = product();
        prepareCustomAttributeSaves();
        when(optionRepository.findByAttribute_IdAndStatusOrderByDisplayOrderAsc(anyString(), any()))
                .thenReturn(List.of());
        when(optionRepository.save(any(ProductAttributeOption.class))).thenAnswer(invocation -> {
            ProductAttributeOption option = invocation.getArgument(0);
            option.setId("OPTION-" + option.getValue());
            return option;
        });
        String json = """
                [{"name":"Kieu dong goi","dataType":"SINGLE_SELECT","optionValues":["Hop","Tui"],"selectedOptionValues":["Hop"]}]
                """;

        service.replaceProductAttributes(product, "SELLER-A", json);

        verify(valueOptionRepository).save(any());
    }

    private void prepareCustomAttributeSaves() {
        when(suggestionRepository.findByCategory_IdAndStatusOrderByDisplayOrderAsc("CAT-1", EntityStatus.ACTIVE))
                .thenReturn(List.of());
        when(valueRepository.findByProduct_IdOrderByDisplayOrderAsc("PRODUCT-1")).thenReturn(List.of());
        when(definitionRepository.save(any(ProductAttributeDefinition.class))).thenAnswer(invocation -> {
            ProductAttributeDefinition definition = invocation.getArgument(0);
            definition.setId("ATTR-NEW");
            return definition;
        });
        when(valueRepository.save(any(ProductAttributeValue.class))).thenAnswer(invocation -> {
            ProductAttributeValue value = invocation.getArgument(0);
            value.setId("VALUE-1");
            return value;
        });
    }

    private Product product() {
        Product product = new Product();
        product.setId("PRODUCT-1");
        product.setCategory(category("CAT-1"));
        return product;
    }

    private Category category(String id) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(EntityStatus.ACTIVE);
        return category;
    }

    private ProductAttributeDefinition definition(
            String id,
            String name,
            String sellerId,
            AttributeNormalizationStatus normalizationStatus
    ) {
        ProductAttributeDefinition definition = new ProductAttributeDefinition();
        definition.setId(id);
        definition.setName(name);
        definition.setNormalizedName(DynamicAttributeService.normalize(name));
        definition.setDataType(AttributeDataType.TEXT);
        definition.setCreatorSellerId(sellerId);
        definition.setNormalizationStatus(normalizationStatus);
        definition.setStatus(EntityStatus.ACTIVE);
        return definition;
    }

    private CategoryAttributeSuggestion suggestion(Category category, ProductAttributeDefinition definition) {
        CategoryAttributeSuggestion suggestion = new CategoryAttributeSuggestion();
        suggestion.setCategory(category);
        suggestion.setAttribute(definition);
        suggestion.setDisplayOrder(0);
        suggestion.setStatus(EntityStatus.ACTIVE);
        return suggestion;
    }
}
