package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import com.ecommerce.catalog.entity.ProductAttributeValue;
import com.ecommerce.catalog.model.request.ProductAggregateRequest;
import com.ecommerce.catalog.repository.CategoryAttributeSuggestionRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.OutboxEventRepository;
import com.ecommerce.catalog.repository.ProductAttributeDefinitionRepository;
import com.ecommerce.catalog.repository.ProductAttributeOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueRepository;
import com.ecommerce.catalog.repository.ProductImageRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.catalog.repository.ProductVariantAxisRepository;
import com.ecommerce.catalog.repository.ProductVariantAxisValueMappingRepository;
import com.ecommerce.catalog.repository.ProductVariantAxisValueRepository;
import com.ecommerce.catalog.repository.ProductVariantRepository;
import com.ecommerce.catalog.repository.VariantAxisNameSuggestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CatalogProductAttributeRulesTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductImageRepository imageRepository;
    @Mock private ProductAttributeDefinitionRepository definitionRepository;
    @Mock private ProductAttributeOptionRepository optionRepository;
    @Mock private ProductAttributeValueRepository valueRepository;
    @Mock private CategoryAttributeSuggestionRepository categorySuggestionRepository;
    @Mock private VariantAxisNameSuggestionRepository axisSuggestionRepository;
    @Mock private ProductVariantAxisRepository axisRepository;
    @Mock private ProductVariantAxisValueRepository axisValueRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductVariantAxisValueMappingRepository mappingRepository;
    @Mock private OutboxEventRepository outboxRepository;

    private CatalogProductService service;
    private Product product;

    @BeforeEach
    void setUp() {
        service = new CatalogProductService(
                new ProductAggregateValidator(), productRepository, categoryRepository, imageRepository,
                definitionRepository, optionRepository, valueRepository, categorySuggestionRepository,
                axisSuggestionRepository, axisRepository, axisValueRepository, variantRepository,
                mappingRepository, outboxRepository, new ObjectMapper());
        Category category = new Category();
        category.setId("category-1");
        product = new Product();
        product.setId("product-1");
        product.setSellerId("seller-1");
        product.setCategory(category);
        lenient().when(categorySuggestionRepository.findByCategory_IdAndStatusOrderByDisplayOrderAsc(
                "category-1", EntityStatus.ACTIVE)).thenReturn(List.of());
    }

    @Test
    void adminDelistMakesProductInactive() {
        product.setStatus(EntityStatus.ACTIVE);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        service.adminDelist("product-1");

        assertEquals(EntityStatus.INACTIVE, product.getStatus());
        verify(productRepository).save(product);
        verify(outboxRepository).save(any());
    }

    @Test
    void savesTextAndNormalizesDefaultNumberUnit() {
        ProductAttributeDefinition text = definition("text", AttributeDataType.TEXT);
        ProductAttributeDefinition number = definition("number", AttributeDataType.NUMBER);
        number.setDefaultUnit("kg");
        when(definitionRepository.findById("text")).thenReturn(Optional.of(text));
        when(definitionRepository.findById("number")).thenReturn(Optional.of(number));

        ProductAggregateRequest.AttributeInput textInput = input("text", AttributeDataType.TEXT);
        textInput.setValueText("  cotton  ");
        ProductAggregateRequest.AttributeInput numberInput = input("number", AttributeDataType.NUMBER);
        numberInput.setValueNumber(new BigDecimal("1.25"));
        numberInput.setUnit("kg");

        service.saveAttributes(product, List.of(textInput, numberInput));

        ArgumentCaptor<ProductAttributeValue> values = ArgumentCaptor.forClass(ProductAttributeValue.class);
        verify(valueRepository, org.mockito.Mockito.times(2)).save(values.capture());
        assertEquals("cotton", values.getAllValues().get(0).getValueText());
        assertEquals(new BigDecimal("1.25"), values.getAllValues().get(1).getValueNumber());
        assertNull(values.getAllValues().get(1).getUnit());
    }

    @Test
    void storesOnlyRealNumberUnitOverride() {
        ProductAttributeDefinition number = definition("number", AttributeDataType.NUMBER);
        number.setDefaultUnit("kg");
        when(definitionRepository.findById("number")).thenReturn(Optional.of(number));
        ProductAggregateRequest.AttributeInput input = input("number", AttributeDataType.NUMBER);
        input.setValueNumber(BigDecimal.ONE);
        input.setUnit("g");

        service.saveAttributes(product, List.of(input));

        ArgumentCaptor<ProductAttributeValue> value = ArgumentCaptor.forClass(ProductAttributeValue.class);
        verify(valueRepository).save(value.capture());
        assertEquals("g", value.getValue().getUnit());
    }

    @Test
    void createsUnverifiedSharedOptionForSelectOne() {
        ProductAttributeDefinition definition = definition("brand", AttributeDataType.SELECT_ONE);
        when(definitionRepository.findById("brand")).thenReturn(Optional.of(definition));
        when(optionRepository.findByDefinition_IdAndNormalizedValueAndStatus(
                "brand", "new brand", EntityStatus.ACTIVE)).thenReturn(Optional.empty());
        when(optionRepository.save(any())).thenAnswer(invocation -> {
            ProductAttributeOption option = invocation.getArgument(0);
            option.setId("created-option");
            return option;
        });
        ProductAggregateRequest.AttributeInput input = input("brand", AttributeDataType.SELECT_ONE);
        input.setSelectedOptionValues(List.of("New Brand"));

        service.saveAttributes(product, List.of(input));

        ArgumentCaptor<ProductAttributeOption> option = ArgumentCaptor.forClass(ProductAttributeOption.class);
        verify(optionRepository).save(option.capture());
        assertEquals("seller-1", option.getValue().getCreatedBySellerId());
        assertFalse(option.getValue().isVerified());
        ArgumentCaptor<ProductAttributeValue> value = ArgumentCaptor.forClass(ProductAttributeValue.class);
        verify(valueRepository).save(value.capture());
        assertSame(option.getValue(), value.getValue().getOption());
    }

    @Test
    void resolvesExactlyOneMergeLayer() {
        ProductAttributeDefinition source = definition("source", AttributeDataType.TEXT);
        source.setMergedIntoDefinitionId("target");
        ProductAttributeDefinition target = definition("target", AttributeDataType.TEXT);
        when(definitionRepository.findById("source")).thenReturn(Optional.of(source));
        when(definitionRepository.findById("target")).thenReturn(Optional.of(target));
        ProductAggregateRequest.AttributeInput input = input("source", AttributeDataType.TEXT);
        input.setValueText("value");

        service.saveAttributes(product, List.of(input));

        ArgumentCaptor<ProductAttributeValue> value = ArgumentCaptor.forClass(ProductAttributeValue.class);
        verify(valueRepository).save(value.capture());
        assertSame(target, value.getValue().getDefinition());
    }

    @Test
    void rejectsMergeTargetThatIsMergedAgain() {
        ProductAttributeDefinition source = definition("source", AttributeDataType.TEXT);
        source.setMergedIntoDefinitionId("target");
        ProductAttributeDefinition target = definition("target", AttributeDataType.TEXT);
        target.setMergedIntoDefinitionId("another");
        when(definitionRepository.findById("source")).thenReturn(Optional.of(source));
        when(definitionRepository.findById("target")).thenReturn(Optional.of(target));
        ProductAggregateRequest.AttributeInput input = input("source", AttributeDataType.TEXT);
        input.setValueText("value");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.saveAttributes(product, List.of(input)));
        assertEquals("MERGE_TARGET_MUST_BE_CANONICAL", error.getMessage());
    }

    @Test
    void rejectsHiddenDefinitionAndTypeMismatch() {
        ProductAttributeDefinition hidden = definition("hidden", AttributeDataType.TEXT);
        hidden.setStatus(EntityStatus.INACTIVE);
        when(definitionRepository.findById("hidden")).thenReturn(Optional.of(hidden));
        ProductAggregateRequest.AttributeInput hiddenInput = input("hidden", AttributeDataType.TEXT);
        hiddenInput.setValueText("value");
        assertEquals("ATTRIBUTE_DEFINITION_HIDDEN", assertThrows(IllegalArgumentException.class,
                () -> service.saveAttributes(product, List.of(hiddenInput))).getMessage());

        ProductAttributeDefinition number = definition("number", AttributeDataType.NUMBER);
        when(definitionRepository.findById("number")).thenReturn(Optional.of(number));
        ProductAggregateRequest.AttributeInput mismatch = input("number", AttributeDataType.TEXT);
        mismatch.setValueText("value");
        assertEquals("ATTRIBUTE_TYPE_MISMATCH", assertThrows(IllegalArgumentException.class,
                () -> service.saveAttributes(product, List.of(mismatch))).getMessage());
    }

    @Test
    void rejectsHiddenOption() {
        ProductAttributeDefinition definition = definition("brand", AttributeDataType.SELECT_ONE);
        ProductAttributeOption option = new ProductAttributeOption();
        option.setId("hidden-option");
        option.setDefinition(definition);
        option.setStatus(EntityStatus.INACTIVE);
        when(definitionRepository.findById("brand")).thenReturn(Optional.of(definition));
        when(optionRepository.findById("hidden-option")).thenReturn(Optional.of(option));
        ProductAggregateRequest.AttributeInput input = input("brand", AttributeDataType.SELECT_ONE);
        input.setSelectedOptionIds(List.of("hidden-option"));

        assertEquals("ATTRIBUTE_OPTION_HIDDEN", assertThrows(IllegalArgumentException.class,
                () -> service.saveAttributes(product, List.of(input))).getMessage());
    }

    @Test
    void rejectsMissingCategoryRequiredAttribute() {
        ProductAttributeDefinition required = definition("required", AttributeDataType.TEXT);
        CategoryAttributeSuggestion suggestion = new CategoryAttributeSuggestion();
        suggestion.setDefinition(required);
        suggestion.setRequiredValue(true);
        when(categorySuggestionRepository.findByCategory_IdAndStatusOrderByDisplayOrderAsc(
                "category-1", EntityStatus.ACTIVE)).thenReturn(List.of(suggestion));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.saveAttributes(product, List.of()));
        assertEquals("CATEGORY_REQUIRED_ATTRIBUTE_MISSING", error.getMessage());
    }

    @Test
    void sellerCannotUpdateAnotherShopsProduct() {
        Product ownedByOtherSeller = new Product();
        ownedByOtherSeller.setId("other-product");
        ownedByOtherSeller.setSellerId("seller-2");
        when(productRepository.findLockedById("other-product")).thenReturn(Optional.of(ownedByOtherSeller));
        ProductAggregateRequest request = new ProductAggregateRequest();
        request.setCategoryId("category-1");
        request.setName("Product");
        ProductAggregateRequest.VariantInput variant = new ProductAggregateRequest.VariantInput();
        variant.setSku("SKU-1");
        variant.setSalePrice(BigDecimal.TEN);
        variant.setQuantity(1);
        variant.setDefaultVariant(true);
        request.setVariants(List.of(variant));

        SecurityException error = assertThrows(SecurityException.class,
                () -> service.update("seller-1", "other-product", request));
        assertEquals("SELLER_PRODUCT_FORBIDDEN", error.getMessage());
    }

    private static ProductAttributeDefinition definition(String id, AttributeDataType type) {
        ProductAttributeDefinition definition = new ProductAttributeDefinition();
        definition.setId(id);
        definition.setDataType(type);
        definition.setStatus(EntityStatus.ACTIVE);
        return definition;
    }

    private static ProductAggregateRequest.AttributeInput input(String definitionId, AttributeDataType type) {
        ProductAggregateRequest.AttributeInput input = new ProductAggregateRequest.AttributeInput();
        input.setDefinitionId(definitionId);
        input.setDataType(type);
        return input;
    }
}
