package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import com.ecommerce.catalog.entity.ProductAttributeValue;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogPublicProductSearchTest {

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
    private Product phone;
    private Product shirt;

    @BeforeEach
    void setUp() {
        service = new CatalogProductService(
                new ProductAggregateValidator(), productRepository, categoryRepository, imageRepository,
                definitionRepository, optionRepository, valueRepository, categorySuggestionRepository,
                axisSuggestionRepository, axisRepository, axisValueRepository, variantRepository,
                mappingRepository, outboxRepository, new ObjectMapper());

        phone = product("phone", "Điện thoại", category("phones", "Điện thoại"), 200L);
        shirt = product("shirt", "Áo cotton", category("shirts", "Áo"), 100L);
        ProductAttributeValue chip = selectedValue(phone, "attr-chip", "option-snapdragon");
        ProductAttributeValue material = textValue(shirt, "attr-material", "Cotton hữu cơ");
        ProductVariant phoneVariant = variant(phone, "15000000");
        ProductVariant shirtVariant = variant(shirt, "500000");

        when(productRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE)).thenReturn(List.of(phone, shirt));
        when(valueRepository.findByProduct_IdOrderByDisplayOrderAsc("phone")).thenReturn(List.of(chip));
        when(valueRepository.findByProduct_IdOrderByDisplayOrderAsc("shirt")).thenReturn(List.of(material));
        when(variantRepository.findByProduct_IdAndStatusOrderByCreatedDateDesc("phone", EntityStatus.ACTIVE)).thenReturn(List.of(phoneVariant));
        when(variantRepository.findByProduct_IdAndStatusOrderByCreatedDateDesc("shirt", EntityStatus.ACTIVE)).thenReturn(List.of(shirtVariant));
        when(imageRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc("phone", EntityStatus.ACTIVE)).thenReturn(List.of());
        when(imageRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc("shirt", EntityStatus.ACTIVE)).thenReturn(List.of());
        when(axisRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc("phone", EntityStatus.ACTIVE)).thenReturn(List.of());
        when(axisRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc("shirt", EntityStatus.ACTIVE)).thenReturn(List.of());
    }

    @Test
    void filtersTwoIndustriesUsingTheirOwnDynamicAttributes() {
        ProductSearchRequest phones = request("phones", "{\"attr-chip\":{\"values\":[\"option-snapdragon\"]}}", "10000000", "20000000");
        ProductSearchRequest shirts = request("shirts", "{\"attr-material\":{\"values\":[\"cotton\"]}}", null, null);

        assertEquals(List.of("phone"), service.publicProducts(phones).getContent().stream().map(row -> row.get("id")).toList());
        assertEquals(List.of("shirt"), service.publicProducts(shirts).getContent().stream().map(row -> row.get("id")).toList());
    }

    private static ProductSearchRequest request(String categoryId, String filters, String min, String max) {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setCategoryId(categoryId);
        request.setAttributeFilters(filters);
        request.setMinPrice(min == null ? null : new BigDecimal(min));
        request.setMaxPrice(max == null ? null : new BigDecimal(max));
        request.setSize(20);
        return request;
    }

    private static Category category(String id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setCode(id.toUpperCase());
        category.setName(name);
        category.setSlug(id);
        return category;
    }

    private static Product product(String id, String name, Category category, long createdDate) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSellerId("seller-" + id);
        product.setCategory(category);
        product.setStatus(EntityStatus.ACTIVE);
        product.setCreatedDate(createdDate);
        return product;
    }

    private static ProductAttributeDefinition definition(String id, AttributeDataType type) {
        ProductAttributeDefinition definition = new ProductAttributeDefinition();
        definition.setId(id);
        definition.setName(id);
        definition.setDataType(type);
        definition.setStatus(EntityStatus.ACTIVE);
        return definition;
    }

    private static ProductAttributeValue selectedValue(Product product, String definitionId, String optionId) {
        ProductAttributeDefinition definition = definition(definitionId, AttributeDataType.SELECT_ONE);
        ProductAttributeOption option = new ProductAttributeOption();
        option.setId(optionId);
        option.setDefinition(definition);
        option.setValue("Snapdragon");
        option.setStatus(EntityStatus.ACTIVE);
        ProductAttributeValue value = new ProductAttributeValue();
        value.setProduct(product);
        value.setDefinition(definition);
        value.setOption(option);
        value.setDisplayOrder(1);
        return value;
    }

    private static ProductAttributeValue textValue(Product product, String definitionId, String text) {
        ProductAttributeValue value = new ProductAttributeValue();
        value.setProduct(product);
        value.setDefinition(definition(definitionId, AttributeDataType.TEXT));
        value.setValueText(text);
        value.setDisplayOrder(1);
        return value;
    }

    private static ProductVariant variant(Product product, String price) {
        ProductVariant variant = new ProductVariant();
        variant.setId("variant-" + product.getId());
        variant.setProduct(product);
        variant.setSku("SKU-" + product.getId());
        variant.setSalePrice(new BigDecimal(price));
        variant.setQuantity(10);
        variant.setStatus(EntityStatus.ACTIVE);
        return variant;
    }
}
