package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import com.ecommerce.catalog.entity.OutboxEvent;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import com.ecommerce.catalog.entity.ProductAttributeValue;
import com.ecommerce.catalog.entity.ProductImage;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.entity.ProductVariantAxis;
import com.ecommerce.catalog.entity.ProductVariantAxisValue;
import com.ecommerce.catalog.entity.ProductVariantAxisValueMapping;
import com.ecommerce.catalog.entity.ProductVariantAxisValueMappingId;
import com.ecommerce.catalog.entity.VariantAxisNameSuggestion;
import com.ecommerce.catalog.model.request.ProductAggregateRequest;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.common.catalog.CatalogVariantSnapshot.VariantSelection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CatalogProductService {

    private final ProductAggregateValidator validator;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAttributeDefinitionRepository definitionRepository;
    private final ProductAttributeOptionRepository optionRepository;
    private final ProductAttributeValueRepository attributeValueRepository;
    private final CategoryAttributeSuggestionRepository categorySuggestionRepository;
    private final VariantAxisNameSuggestionRepository axisSuggestionRepository;
    private final ProductVariantAxisRepository axisRepository;
    private final ProductVariantAxisValueRepository axisValueRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantAxisValueMappingRepository mappingRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public CatalogProductService(
            ProductAggregateValidator validator,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository imageRepository,
            ProductAttributeDefinitionRepository definitionRepository,
            ProductAttributeOptionRepository optionRepository,
            ProductAttributeValueRepository attributeValueRepository,
            CategoryAttributeSuggestionRepository categorySuggestionRepository,
            VariantAxisNameSuggestionRepository axisSuggestionRepository,
            ProductVariantAxisRepository axisRepository,
            ProductVariantAxisValueRepository axisValueRepository,
            ProductVariantRepository variantRepository,
            ProductVariantAxisValueMappingRepository mappingRepository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.validator = validator;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.definitionRepository = definitionRepository;
        this.optionRepository = optionRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.categorySuggestionRepository = categorySuggestionRepository;
        this.axisSuggestionRepository = axisSuggestionRepository;
        this.axisRepository = axisRepository;
        this.axisValueRepository = axisValueRepository;
        this.variantRepository = variantRepository;
        this.mappingRepository = mappingRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> create(String sellerId, ProductAggregateRequest request) {
        requireSeller(sellerId);
        validator.validate(request);
        validateCreateIds(request);
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setCode(request.getCode() == null || request.getCode().isBlank()
                ? "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : request.getCode().trim());
        product.setStatus(EntityStatus.ACTIVE);
        return persistAggregate(product, request, false);
    }

    @Transactional
    public Map<String, Object> update(String sellerId, String productId, ProductAggregateRequest request) {
        requireSeller(sellerId);
        validator.validate(request);
        Product product = productRepository.findLockedById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
        if (!sellerId.equals(product.getSellerId())) {
            throw new SecurityException("SELLER_PRODUCT_FORBIDDEN");
        }
        return persistAggregate(product, request, true);
    }

    private Map<String, Object> persistAggregate(Product product, ProductAggregateRequest request, boolean replacing) {
        Category category = categoryRepository.findByIdAndStatus(request.getCategoryId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("CATEGORY_INVALID"));
        if (categoryRepository.existsByParent_IdAndStatus(category.getId(), EntityStatus.ACTIVE)) {
            throw new IllegalArgumentException("CATEGORY_MUST_BE_LEAF");
        }

        if (replacing) {
            validateReplacementIds(product.getId(), request);
            clearAggregate(product.getId());
        }
        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        if (request.getCode() != null && !request.getCode().isBlank()) product.setCode(request.getCode().trim());
        product = productRepository.saveAndFlush(product);

        saveImages(product, request.getProductImages());
        saveAttributes(product, request.getAttributes());
        saveAxesAndVariants(product, request.getVariantAxes(), request.getVariants());
        Map<String, Object> response = detail(product.getId());
        enqueue(product.getId(), replacing ? "ProductUpdated" : "ProductCreated", buildSearchDocument(product));
        return response;
    }

    private void clearAggregate(String productId) {
        List<ProductVariant> oldVariants = variantRepository.findByProduct_IdOrderByCreatedDateDesc(productId);
        if (!oldVariants.isEmpty()) {
            List<String> variantIds = oldVariants.stream().map(ProductVariant::getId).toList();
            mappingRepository.deleteByVariant_IdIn(variantIds);
            mappingRepository.flush();
            variantRepository.deleteByProduct_Id(productId);
            variantRepository.flush();
        }
        List<ProductVariantAxis> oldAxes = axisRepository.findByProduct_IdOrderByDisplayOrderAsc(productId);
        if (!oldAxes.isEmpty()) {
            axisValueRepository.deleteByAxis_IdIn(oldAxes.stream().map(ProductVariantAxis::getId).toList());
            axisValueRepository.flush();
            axisRepository.deleteByProduct_Id(productId);
            axisRepository.flush();
        }
        attributeValueRepository.deleteByProduct_Id(productId);
        imageRepository.deleteByProduct_Id(productId);
        attributeValueRepository.flush();
        imageRepository.flush();
    }

    private void saveImages(Product product, List<ProductAggregateRequest.ImageInput> inputs) {
        Set<Integer> orders = new HashSet<>();
        for (ProductAggregateRequest.ImageInput input : safe(inputs)) {
            if (input.getDisplayOrder() == null || !orders.add(input.getDisplayOrder())) {
                throw new IllegalArgumentException("DUPLICATE_PRODUCT_IMAGE_ORDER");
            }
            ProductImage image = new ProductImage();
            reuseId(image, input.getId());
            image.setProduct(product);
            image.setUrl(input.getUrl().trim());
            image.setDisplayOrder(input.getDisplayOrder());
            image.setStatus(input.getStatus() == null ? EntityStatus.ACTIVE : input.getStatus());
            imageRepository.save(image);
        }
    }

    void saveAttributes(Product product, List<ProductAggregateRequest.AttributeInput> inputs) {
        Set<String> singletonDefinitions = new HashSet<>();
        Set<String> suppliedDefinitions = new HashSet<>();
        for (ProductAggregateRequest.AttributeInput input : safe(inputs)) {
            if (input == null || input.getDataType() == null) {
                throw new IllegalArgumentException("ATTRIBUTE_TYPE_REQUIRED");
            }
            ProductAttributeDefinition definition = resolveOrCreateDefinition(product.getSellerId(), input);
            if (definition.getDataType() != input.getDataType()) {
                throw new IllegalArgumentException("ATTRIBUTE_TYPE_MISMATCH");
            }
            if (definition.getDataType() != AttributeDataType.SELECT_MULTI && !singletonDefinitions.add(definition.getId())) {
                throw new IllegalArgumentException("ATTRIBUTE_SINGLETON_VIOLATION");
            }
            suppliedDefinitions.add(definition.getId());
            switch (definition.getDataType()) {
                case TEXT -> saveTextValue(product, definition, input);
                case NUMBER -> saveNumberValue(product, definition, input);
                case SELECT_ONE, SELECT_MULTI -> saveSelectedValues(product, definition, input);
            }
        }
        List<String> missingRequired = categorySuggestionRepository
                .findByCategory_IdAndStatusOrderByDisplayOrderAsc(product.getCategory().getId(), EntityStatus.ACTIVE)
                .stream()
                .filter(CategoryAttributeSuggestion::isRequiredValue)
                .map(suggestion -> suggestion.getDefinition().getId())
                .filter(definitionId -> !suppliedDefinitions.contains(definitionId))
                .toList();
        if (!missingRequired.isEmpty()) {
            throw new IllegalArgumentException("CATEGORY_REQUIRED_ATTRIBUTE_MISSING");
        }
    }

    private ProductAttributeDefinition resolveOrCreateDefinition(String sellerId, ProductAggregateRequest.AttributeInput input) {
        ProductAttributeDefinition definition;
        if (input.getDefinitionId() != null && !input.getDefinitionId().isBlank()) {
            definition = definitionRepository.findById(input.getDefinitionId())
                    .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_DEFINITION_NOT_FOUND"));
        } else {
            if (input.getName() == null || input.getName().isBlank()) {
                throw new IllegalArgumentException("ATTRIBUTE_NAME_REQUIRED");
            }
            String normalized = ProductAggregateValidator.normalize(input.getName());
            definition = definitionRepository.findByNormalizedNameAndStatus(normalized, EntityStatus.ACTIVE).orElseGet(() -> {
                ProductAttributeDefinition created = new ProductAttributeDefinition();
                created.setCode("ATTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                created.setName(input.getName().trim());
                created.setNormalizedName(normalized);
                created.setDataType(input.getDataType());
                created.setCreatedBySellerId(sellerId);
                created.setVerified(false);
                created.setStatus(EntityStatus.ACTIVE);
                return definitionRepository.save(created);
            });
        }
        if (definition.getStatus() != EntityStatus.ACTIVE) {
            throw new IllegalArgumentException("ATTRIBUTE_DEFINITION_HIDDEN");
        }
        if (definition.getMergedIntoDefinitionId() != null) {
            ProductAttributeDefinition target = definitionRepository.findById(definition.getMergedIntoDefinitionId())
                    .orElseThrow(() -> new IllegalArgumentException("MERGED_ATTRIBUTE_TARGET_NOT_FOUND"));
            if (target.getMergedIntoDefinitionId() != null) {
                throw new IllegalStateException("MERGE_TARGET_MUST_BE_CANONICAL");
            }
            if (target.getStatus() != EntityStatus.ACTIVE) {
                throw new IllegalArgumentException("ATTRIBUTE_DEFINITION_HIDDEN");
            }
            definition = target;
        }
        return definition;
    }

    private void saveTextValue(Product product, ProductAttributeDefinition definition, ProductAggregateRequest.AttributeInput input) {
        if (input.getValueText() == null || input.getValueText().isBlank()) {
            throw new IllegalArgumentException("ATTRIBUTE_TEXT_VALUE_REQUIRED");
        }
        ProductAttributeValue value = baseValue(product, definition, input.getDisplayOrder());
        value.setValueText(input.getValueText().trim());
        attributeValueRepository.save(value);
    }

    private void saveNumberValue(Product product, ProductAttributeDefinition definition, ProductAggregateRequest.AttributeInput input) {
        if (input.getValueNumber() == null) throw new IllegalArgumentException("ATTRIBUTE_NUMBER_VALUE_REQUIRED");
        ProductAttributeValue value = baseValue(product, definition, input.getDisplayOrder());
        value.setValueNumber(input.getValueNumber());
        String unit = blankToNull(input.getUnit());
        value.setUnit(Objects.equals(unit, blankToNull(definition.getDefaultUnit())) ? null : unit);
        attributeValueRepository.save(value);
    }

    private void saveSelectedValues(Product product, ProductAttributeDefinition definition, ProductAggregateRequest.AttributeInput input) {
        List<ProductAttributeOption> selected = new ArrayList<>();
        for (String optionId : safe(input.getSelectedOptionIds())) {
            ProductAttributeOption option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_OPTION_NOT_FOUND"));
            selected.add(resolveOption(definition, option));
        }
        for (String optionValue : safe(input.getSelectedOptionValues())) {
            if (optionValue == null || optionValue.isBlank()) continue;
            String normalized = ProductAggregateValidator.normalize(optionValue);
            ProductAttributeOption option = optionRepository
                    .findByDefinition_IdAndNormalizedValueAndStatus(definition.getId(), normalized, EntityStatus.ACTIVE)
                    .orElseGet(() -> {
                        ProductAttributeOption created = new ProductAttributeOption();
                        created.setDefinition(definition);
                        created.setValue(optionValue.trim());
                        created.setNormalizedValue(normalized);
                        created.setCreatedBySellerId(product.getSellerId());
                        created.setVerified(false);
                        created.setDisplayOrder(0);
                        created.setStatus(EntityStatus.ACTIVE);
                        return optionRepository.save(created);
                    });
            selected.add(resolveOption(definition, option));
        }
        selected = selected.stream().collect(Collectors.toMap(ProductAttributeOption::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new)).values().stream().toList();
        if ((definition.getDataType() == AttributeDataType.SELECT_ONE && selected.size() != 1)
                || (definition.getDataType() == AttributeDataType.SELECT_MULTI && selected.isEmpty())) {
            throw new IllegalArgumentException("ATTRIBUTE_OPTION_SELECTION_INVALID");
        }
        for (ProductAttributeOption option : selected) {
            ProductAttributeValue value = baseValue(product, definition, input.getDisplayOrder());
            value.setOption(option);
            attributeValueRepository.save(value);
        }
    }

    private ProductAttributeOption resolveOption(ProductAttributeDefinition definition, ProductAttributeOption option) {
        if (option.getStatus() != EntityStatus.ACTIVE) {
            throw new IllegalArgumentException("ATTRIBUTE_OPTION_HIDDEN");
        }
        if (!definition.getId().equals(option.getDefinition().getId())) {
            throw new IllegalArgumentException("ATTRIBUTE_OPTION_DEFINITION_MISMATCH");
        }
        if (option.getMergedIntoOptionId() != null) {
            ProductAttributeOption target = optionRepository.findById(option.getMergedIntoOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("MERGED_OPTION_TARGET_NOT_FOUND"));
            if (target.getMergedIntoOptionId() != null || !definition.getId().equals(target.getDefinition().getId())) {
                throw new IllegalStateException("MERGE_OPTION_TARGET_MUST_BE_CANONICAL");
            }
            if (target.getStatus() != EntityStatus.ACTIVE) {
                throw new IllegalArgumentException("ATTRIBUTE_OPTION_HIDDEN");
            }
            return target;
        }
        return option;
    }

    private ProductAttributeValue baseValue(Product product, ProductAttributeDefinition definition, Integer displayOrder) {
        ProductAttributeValue value = new ProductAttributeValue();
        value.setProduct(product);
        value.setDefinition(definition);
        value.setDisplayOrder(displayOrder == null ? 0 : displayOrder);
        value.setStatus(EntityStatus.ACTIVE);
        return value;
    }

    private void saveAxesAndVariants(
            Product product,
            List<ProductAggregateRequest.AxisInput> axisInputs,
            List<ProductAggregateRequest.VariantInput> variantInputs
    ) {
        Map<String, ProductVariantAxisValue> valuesByClientKey = new HashMap<>();
        Map<String, ProductVariantAxis> axesByClientKey = new HashMap<>();
        for (ProductAggregateRequest.AxisInput input : safe(axisInputs).stream()
                .sorted(Comparator.comparing(ProductAggregateRequest.AxisInput::getDisplayOrder)).toList()) {
            ProductVariantAxis axis = new ProductVariantAxis();
            reuseId(axis, input.getId());
            axis.setProduct(product);
            axis.setName(input.getName().trim());
            axis.setNormalizedName(ProductAggregateValidator.normalize(input.getName()));
            axis.setDisplayOrder(input.getDisplayOrder());
            axis.setStatus(EntityStatus.ACTIVE);
            if (input.getNameSuggestionId() != null) {
                axis.setNameSuggestion(axisSuggestionRepository.findById(input.getNameSuggestionId())
                        .orElseThrow(() -> new IllegalArgumentException("AXIS_SUGGESTION_NOT_FOUND")));
            }
            axis = axisRepository.save(axis);
            axesByClientKey.put(input.getClientKey(), axis);
            for (ProductAggregateRequest.AxisValueInput valueInput : safe(input.getValues())) {
                ProductVariantAxisValue value = new ProductVariantAxisValue();
                reuseId(value, valueInput.getId());
                value.setAxis(axis);
                value.setValue(valueInput.getValue().trim());
                value.setNormalizedValue(ProductAggregateValidator.normalize(valueInput.getValue()));
                value.setDisplayOrder(valueInput.getDisplayOrder());
                value.setStatus(EntityStatus.ACTIVE);
                valuesByClientKey.put(valueInput.getClientKey(), axisValueRepository.save(value));
            }
        }
        axisValueRepository.flush();

        List<ProductVariantAxis> orderedAxes = axesByClientKey.values().stream()
                .sorted(Comparator.comparing(ProductVariantAxis::getDisplayOrder)).toList();
        for (ProductAggregateRequest.VariantInput input : safe(variantInputs)) {
            List<ProductVariantAxisValue> selected = safe(input.getSelectionValueKeys()).stream()
                    .map(valuesByClientKey::get)
                    .toList();
            if (selected.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("VARIANT_MAPPING_INVALID");
            ProductVariant variant = new ProductVariant();
            reuseId(variant, input.getId());
            variant.setProduct(product);
            variant.setSku(input.getSku().trim());
            variant.setSalePrice(input.getSalePrice());
            variant.setQuantity(input.getQuantity());
            variant.setImageUrl(blankToNull(input.getImageUrl()));
            variant.setDefaultVariant(input.isDefaultVariant());
            variant.setStatus(input.getStatus() == null ? EntityStatus.ACTIVE : input.getStatus());
            String combinationKey = orderedAxes.isEmpty() ? "DEFAULT" : orderedAxes.stream()
                    .map(axis -> selected.stream().filter(value -> value.getAxis().getId().equals(axis.getId())).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("VARIANT_MAPPING_INVALID")).getId())
                    .collect(Collectors.joining("|"));
            variant.setCombinationKey(combinationKey);
            variant = variantRepository.saveAndFlush(variant);
            for (ProductVariantAxisValue value : selected) {
                ProductVariantAxisValueMapping mapping = new ProductVariantAxisValueMapping();
                mapping.setId(new ProductVariantAxisValueMappingId(variant.getId(), value.getId()));
                mapping.setVariant(variant);
                mapping.setAxisValue(value);
                mappingRepository.save(mapping);
            }
        }
        mappingRepository.flush();
    }

    private void validateReplacementIds(String productId, ProductAggregateRequest request) {
        Set<String> existingImageIds = imageRepository.findByProduct_IdOrderByDisplayOrderAsc(productId).stream()
                .map(ProductImage::getId).collect(Collectors.toSet());
        List<ProductVariantAxis> existingAxes = axisRepository.findByProduct_IdOrderByDisplayOrderAsc(productId);
        Set<String> existingAxisIds = existingAxes.stream().map(ProductVariantAxis::getId).collect(Collectors.toSet());
        Set<String> existingValueIds = existingAxes.isEmpty() ? Set.of() : axisValueRepository
                .findByAxis_IdIn(existingAxisIds).stream().map(ProductVariantAxisValue::getId).collect(Collectors.toSet());
        Set<String> existingVariantIds = variantRepository.findByProduct_IdOrderByCreatedDateDesc(productId).stream()
                .map(ProductVariant::getId).collect(Collectors.toSet());

        validateReusableIds(safe(request.getProductImages()).stream().map(ProductAggregateRequest.ImageInput::getId).toList(),
                existingImageIds, "PRODUCT_IMAGE_ID_INVALID");
        validateReusableIds(safe(request.getVariantAxes()).stream().map(ProductAggregateRequest.AxisInput::getId).toList(),
                existingAxisIds, "VARIANT_AXIS_ID_INVALID");
        validateReusableIds(safe(request.getVariantAxes()).stream().flatMap(axis -> safe(axis.getValues()).stream())
                        .map(ProductAggregateRequest.AxisValueInput::getId).toList(),
                existingValueIds, "VARIANT_AXIS_VALUE_ID_INVALID");
        validateReusableIds(safe(request.getVariants()).stream().map(ProductAggregateRequest.VariantInput::getId).toList(),
                existingVariantIds, "PRODUCT_VARIANT_ID_INVALID");
    }

    private void validateCreateIds(ProductAggregateRequest request) {
        validateNoIds(safe(request.getProductImages()).stream().map(ProductAggregateRequest.ImageInput::getId).toList());
        validateNoIds(safe(request.getVariantAxes()).stream().map(ProductAggregateRequest.AxisInput::getId).toList());
        validateNoIds(safe(request.getVariantAxes()).stream().flatMap(axis -> safe(axis.getValues()).stream())
                .map(ProductAggregateRequest.AxisValueInput::getId).toList());
        validateNoIds(safe(request.getVariants()).stream().map(ProductAggregateRequest.VariantInput::getId).toList());
    }

    private void validateNoIds(List<String> requestedIds) {
        if (requestedIds.stream().anyMatch(id -> id != null && !id.isBlank())) {
            throw new IllegalArgumentException("PRODUCT_CREATE_ID_NOT_ALLOWED");
        }
    }

    private void validateReusableIds(List<String> requestedIds, Set<String> existingIds, String error) {
        Set<String> unique = new HashSet<>();
        for (String id : requestedIds) {
            if (id != null && !id.isBlank() && (!existingIds.contains(id) || !unique.add(id))) {
                throw new IllegalArgumentException(error);
            }
        }
    }

    private void reuseId(com.ecommerce.catalog.entity.base.PrimaryEntity entity, String id) {
        if (id != null && !id.isBlank()) entity.setId(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
        return buildDetail(product);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> sellerProducts(String sellerId, ProductSearchRequest request) {
        requireSeller(sellerId);
        return productRepository.findBySellerIdAndNameContainingIgnoreCaseOrderByCreatedDateDesc(
                sellerId, safeQuery(request.getQ()), page(request)).map(this::buildSummary);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> publicProducts(ProductSearchRequest request) {
        Map<String, AttributeCriterion> attributeFilters = parseAttributeFilters(request.getAttributeFilters());
        String query = ProductAggregateValidator.normalize(safeQuery(request.getQ()));
        List<Product> matches = productRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream()
                .filter(product -> request.getSellerId() == null || request.getSellerId().isBlank()
                        || request.getSellerId().equals(product.getSellerId()))
                .filter(product -> request.getCategoryId() == null || request.getCategoryId().isBlank()
                        || request.getCategoryId().equals(product.getCategory().getId()))
                .filter(product -> query.isBlank() || ProductAggregateValidator.normalize(product.getName()).contains(query))
                .filter(product -> matchesAttributeFilters(product.getId(), attributeFilters))
                .filter(product -> matchesPrice(product.getId(), request.getMinPrice(), request.getMaxPrice()))
                .sorted(productComparator(request.getSort()))
                .toList();

        PageRequest pageable = page(request);
        int from = Math.min((int) pageable.getOffset(), matches.size());
        int to = Math.min(from + pageable.getPageSize(), matches.size());
        List<Map<String, Object>> content = matches.subList(from, to).stream().map(this::buildSummary).toList();
        return new PageImpl<>(content, pageable, matches.size());
    }

    private Map<String, AttributeCriterion> parseAttributeFilters(String rawFilters) {
        if (rawFilters == null || rawFilters.isBlank()) return Map.of();
        try {
            JsonNode root = objectMapper.readTree(rawFilters);
            if (!root.isObject()) throw new IllegalArgumentException("ATTRIBUTE_FILTERS_INVALID");
            Map<String, AttributeCriterion> result = new LinkedHashMap<>();
            root.fields().forEachRemaining(entry -> {
                JsonNode criterion = entry.getValue();
                if (!criterion.isObject()) throw new IllegalArgumentException("ATTRIBUTE_FILTERS_INVALID");
                List<String> values = new ArrayList<>();
                JsonNode valueNode = criterion.get("values");
                if (valueNode != null && valueNode.isArray()) {
                    valueNode.forEach(value -> {
                        if (value.isTextual() && !value.asText().isBlank()) values.add(value.asText().trim());
                    });
                }
                BigDecimal min = decimalOrNull(criterion.get("min"));
                BigDecimal max = decimalOrNull(criterion.get("max"));
                if (!values.isEmpty() || min != null || max != null) {
                    result.put(entry.getKey(), new AttributeCriterion(values, min, max));
                }
            });
            return result;
        } catch (JsonProcessingException | NumberFormatException exception) {
            throw new IllegalArgumentException("ATTRIBUTE_FILTERS_INVALID", exception);
        }
    }

    private static BigDecimal decimalOrNull(JsonNode node) {
        return node == null || node.isNull() || node.asText().isBlank() ? null : new BigDecimal(node.asText());
    }

    private boolean matchesAttributeFilters(String productId, Map<String, AttributeCriterion> filters) {
        if (filters.isEmpty()) return true;
        Map<String, List<ProductAttributeValue>> valuesByDefinition = attributeValueRepository
                .findByProduct_IdOrderByDisplayOrderAsc(productId).stream()
                .collect(Collectors.groupingBy(value -> value.getDefinition().getId()));
        return filters.entrySet().stream().allMatch(entry -> {
            List<ProductAttributeValue> values = valuesByDefinition.getOrDefault(entry.getKey(), List.of());
            AttributeCriterion criterion = entry.getValue();
            boolean valuesMatch = criterion.values().isEmpty() || values.stream().anyMatch(value -> {
                ProductAttributeOption option = value.getOption();
                if (option != null) {
                    String resolvedId = option.getMergedIntoOptionId() == null ? option.getId() : option.getMergedIntoOptionId();
                    return criterion.values().contains(option.getId()) || criterion.values().contains(resolvedId);
                }
                String text = ProductAggregateValidator.normalize(value.getValueText());
                return !text.isBlank() && criterion.values().stream()
                        .map(ProductAggregateValidator::normalize).anyMatch(text::contains);
            });
            boolean rangeMatches = criterion.min() == null && criterion.max() == null || values.stream()
                    .map(ProductAttributeValue::getValueNumber).filter(Objects::nonNull)
                    .anyMatch(value -> (criterion.min() == null || value.compareTo(criterion.min()) >= 0)
                            && (criterion.max() == null || value.compareTo(criterion.max()) <= 0));
            return valuesMatch && rangeMatches;
        });
    }

    private boolean matchesPrice(String productId, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) return true;
        return variantRepository.findByProduct_IdAndStatusOrderByCreatedDateDesc(productId, EntityStatus.ACTIVE).stream()
                .map(ProductVariant::getSalePrice)
                .anyMatch(price -> (minPrice == null || price.compareTo(minPrice) >= 0)
                        && (maxPrice == null || price.compareTo(maxPrice) <= 0));
    }

    private Comparator<Product> productComparator(String sort) {
        Comparator<Product> newest = Comparator.comparing(Product::getCreatedDate,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        return switch (sort == null ? "createdAt_desc" : sort) {
            case "createdAt_asc" -> Comparator.comparing(Product::getCreatedDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "price_asc" -> Comparator.comparing(this::minimumActivePrice,
                    Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(newest);
            case "price_desc" -> Comparator.comparing(this::minimumActivePrice,
                    Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(newest);
            default -> newest;
        };
    }

    private BigDecimal minimumActivePrice(Product product) {
        return variantRepository.findByProduct_IdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE)
                .stream().map(ProductVariant::getSalePrice).min(BigDecimal::compareTo).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> activeProductSummaries() {
        return productRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream().map(this::buildSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogVariantSnapshot> productVariantSnapshots(String productId) {
        return variantRepository.findByProduct_IdAndStatusOrderByCreatedDateDesc(productId, EntityStatus.ACTIVE)
                .stream().map(this::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> sellerLowStockVariants(String sellerId, int threshold) {
        int safeThreshold = Math.max(threshold, 0);
        return variantRepository
                .findByProduct_SellerIdAndProduct_StatusAndStatusAndQuantityLessThanEqualOrderByQuantityAsc(
                        sellerId, EntityStatus.ACTIVE, EntityStatus.ACTIVE, safeThreshold)
                .stream()
                .map(variant -> {
                    Map<String, Object> row = linkedMap();
                    row.put("id", variant.getId());
                    row.put("code", variant.getSku());
                    row.put("productName", variant.getProduct().getName());
                    row.put("variantLabel", snapshot(variant).variantLabel());
                    row.put("quantity", variant.getQuantity());
                    row.put("imageUrl", variant.getImageUrl());
                    return row;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> categoryTree() {
        return categoryRepository.findByParentIsNullAndStatusOrderByDisplayOrderAsc(EntityStatus.ACTIVE).stream()
                .map(this::categoryNode).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> attributeSuggestions(String categoryId, String q) {
        String normalized = ProductAggregateValidator.normalize(q);
        return categorySuggestionRepository.findByCategory_IdAndStatusOrderByDisplayOrderAsc(categoryId, EntityStatus.ACTIVE).stream()
                .filter(link -> normalized.isBlank() || link.getDefinition().getNormalizedName().contains(normalized))
                .map(link -> definitionMap(link.getDefinition(), link)).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> axisNameSuggestions(String q) {
        return axisSuggestionRepository
                .findByNormalizedNameContainingIgnoreCaseAndStatusOrderByVerifiedDescNameAsc(
                        ProductAggregateValidator.normalize(q), EntityStatus.ACTIVE)
                .stream().map(this::axisSuggestionMap).toList();
    }

    @Transactional(readOnly = true)
    public CatalogVariantSnapshot variantSnapshot(String variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_VARIANT_NOT_FOUND"));
        return snapshot(variant);
    }

    @Transactional(readOnly = true)
    public List<CatalogVariantSnapshot> variantSnapshots(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return variantRepository.findByIdIn(new ArrayList<>(ids)).stream().map(this::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogVariantSnapshot> searchVariantSnapshots(
            String q, String status, String productId, BigDecimal minPrice, BigDecimal maxPrice) {
        String normalizedQuery = ProductAggregateValidator.normalize(q);
        return productRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream()
                .filter(product -> productId == null || productId.isBlank() || productId.equals(product.getId()))
                .flatMap(product -> variantRepository.findByProduct_IdOrderByCreatedDateDesc(product.getId()).stream())
                .map(this::snapshot)
                .filter(snapshot -> status == null || status.isBlank() || status.equalsIgnoreCase(snapshot.status()))
                .filter(snapshot -> minPrice == null || snapshot.salePrice().compareTo(minPrice) >= 0)
                .filter(snapshot -> maxPrice == null || snapshot.salePrice().compareTo(maxPrice) <= 0)
                .filter(snapshot -> normalizedQuery.isBlank()
                        || ProductAggregateValidator.normalize(snapshot.productName()).contains(normalizedQuery)
                        || ProductAggregateValidator.normalize(snapshot.sku()).contains(normalizedQuery)
                        || ProductAggregateValidator.normalize(snapshot.variantLabel()).contains(normalizedQuery))
                .toList();
    }

    @Transactional
    public CatalogVariantSnapshot adjustStock(String variantId, int delta) {
        ProductVariant variant = variantRepository.findLockedById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_VARIANT_NOT_FOUND"));
        int next = variant.getQuantity() + delta;
        if (next < 0) throw new IllegalArgumentException("INSUFFICIENT_STOCK");
        variant.setQuantity(next);
        variantRepository.save(variant);
        enqueue(variant.getProduct().getId(), "ProductUpdated", buildSearchDocument(variant.getProduct()));
        return snapshot(variant);
    }

    @Transactional
    public Map<String, Object> updateRating(String productId, double average, long count) {
        if (!Double.isFinite(average) || average < 0D || average > 5D || count < 0L) {
            throw new IllegalArgumentException("PRODUCT_RATING_INVALID");
        }
        Product product = productRepository.findLockedById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
        product.setRatingAverage(java.math.BigDecimal.valueOf(average));
        product.setRatingCount(count);
        productRepository.save(product);
        enqueue(productId, "ProductUpdated", buildSearchDocument(product));
        return buildDetail(product);
    }

    @Transactional
    public Map<String, Object> changeStatus(String sellerId, String productId, EntityStatus status) {
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new SecurityException("SELLER_PRODUCT_FORBIDDEN"));
        product.setStatus(status);
        productRepository.save(product);
        Map<String, Object> detail = buildDetail(product);
        enqueue(productId, status == EntityStatus.ACTIVE ? "ProductUpdated" : "ProductDeleted",
                status == EntityStatus.ACTIVE ? buildSearchDocument(product) : null);
        return detail;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> internalProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
        return buildDetail(product);
    }

    @Transactional
    public Map<String, Object> adminDelist(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
        if (product.getStatus() != EntityStatus.INACTIVE) {
            product.setStatus(EntityStatus.INACTIVE);
            productRepository.save(product);
            enqueue(productId, "ProductDeleted", null);
        }
        return buildDetail(product);
    }

    @Transactional
    public Map<String, Object> reindexAll() {
        List<Product> products = productRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE);
        products.forEach(product -> enqueue(product.getId(), "ProductUpdated", buildSearchDocument(product)));
        return Map.of("enqueued", products.size());
    }

    private Map<String, Object> buildDetail(Product product) {
        Map<String, Object> result = linkedMap();
        result.put("id", product.getId());
        result.put("code", product.getCode());
        result.put("sellerId", product.getSellerId());
        result.put("name", product.getName());
        result.put("description", product.getDescription());
        result.put("status", product.getStatus());
        result.put("ratingAverage", product.getRatingAverage());
        result.put("ratingCount", product.getRatingCount());
        result.put("category", categoryRef(product.getCategory()));
        result.put("productImages", imageRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc(product.getId(), EntityStatus.ACTIVE)
                .stream().map(this::imageMap).toList());
        result.put("attributes", attributeMaps(product.getId()));

        List<ProductVariantAxis> axes = axisRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc(product.getId(), EntityStatus.ACTIVE);
        result.put("variantAxes", axes.stream().map(this::axisMap).toList());
        List<ProductVariant> variants = variantRepository.findByProduct_IdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE);
        result.put("variants", variants.stream().map(this::variantMap).toList());
        return result;
    }

    private Map<String, Object> buildSummary(Product product) {
        List<ProductVariant> variants = variantRepository.findByProduct_IdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE);
        List<BigDecimal> prices = variants.stream().map(ProductVariant::getSalePrice).toList();
        List<ProductImage> images = imageRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc(product.getId(), EntityStatus.ACTIVE);
        Map<String, Object> result = linkedMap();
        result.put("id", product.getId());
        result.put("sellerId", product.getSellerId());
        result.put("name", product.getName());
        result.put("status", product.getStatus());
        result.put("category", categoryRef(product.getCategory()));
        result.put("minPrice", prices.stream().min(BigDecimal::compareTo).orElse(null));
        result.put("maxPrice", prices.stream().max(BigDecimal::compareTo).orElse(null));
        result.put("totalQuantity", variants.stream().mapToInt(ProductVariant::getQuantity).sum());
        result.put("activeVariantCount", variants.size());
        result.put("thumbnailUrl", !images.isEmpty() ? images.get(0).getUrl() : variants.stream().map(ProductVariant::getImageUrl).filter(Objects::nonNull).findFirst().orElse(null));
        result.put("ratingAverage", product.getRatingAverage());
        result.put("ratingCount", product.getRatingCount());
        result.put("attributePreview", attributeMaps(product.getId()).stream().limit(4).toList());
        result.put("axisPreview", axisRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc(product.getId(), EntityStatus.ACTIVE).stream().map(this::axisMap).toList());
        return result;
    }

    private Map<String, Object> buildSearchDocument(Product product) {
        Map<String, Object> document = linkedMap();
        document.put("id", product.getId());
        document.put("code", product.getCode());
        document.put("sellerId", product.getSellerId());
        document.put("name", product.getName());
        document.put("description", product.getDescription());
        document.put("status", product.getStatus().name());
        document.put("categoryId", product.getCategory().getId());
        document.put("categoryName", product.getCategory().getName());
        document.put("categorySlug", product.getCategory().getSlug());
        List<ProductImage> images = imageRepository.findByProduct_IdAndStatusOrderByDisplayOrderAsc(
                product.getId(), EntityStatus.ACTIVE);
        document.put("imageUrl", images.isEmpty() ? null : images.get(0).getUrl());
        document.put("ratingAverage", product.getRatingAverage());
        document.put("ratingCount", product.getRatingCount());
        return document;
    }

    private List<Map<String, Object>> searchAttributeMaps(String productId) {
        List<Map<String, Object>> searchAttributes = new ArrayList<>();
        for (Map<String, Object> attribute : attributeMaps(productId)) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> options = (List<Map<String, Object>>) attribute.get("selectedOptions");
            if (options == null || options.isEmpty()) {
                searchAttributes.add(searchAttributeMap(attribute, null));
            } else {
                for (Map<String, Object> option : options) {
                    searchAttributes.add(searchAttributeMap(attribute, option));
                }
            }
        }
        return searchAttributes;
    }

    private Map<String, Object> searchAttributeMap(Map<String, Object> attribute, Map<String, Object> option) {
        Map<String, Object> searchAttribute = linkedMap();
        searchAttribute.put("definitionId", attribute.get("definitionId"));
        searchAttribute.put("name", attribute.get("name"));
        searchAttribute.put("dataType", attribute.get("dataType"));
        searchAttribute.put("valueText", attribute.get("valueText"));
        searchAttribute.put("valueNumber", attribute.get("valueNumber"));
        searchAttribute.put("unit", attribute.get("unit"));
        searchAttribute.put("optionIds", option == null ? null : option.get("resolvedOptionId"));
        searchAttribute.put("optionValues", option == null ? null : option.get("value"));
        return searchAttribute;
    }

    private List<Map<String, Object>> attributeMaps(String productId) {
        List<ProductAttributeValue> values = attributeValueRepository.findByProduct_IdOrderByDisplayOrderAsc(productId);
        Map<String, List<ProductAttributeValue>> grouped = values.stream().collect(Collectors.groupingBy(
                value -> value.getDefinition().getId(), LinkedHashMap::new, Collectors.toList()));
        return grouped.values().stream().map(group -> {
            ProductAttributeValue first = group.get(0);
            ProductAttributeDefinition definition = first.getDefinition();
            Map<String, Object> map = linkedMap();
            map.put("definitionId", definition.getId());
            map.put("name", definition.getName());
            map.put("dataType", definition.getDataType());
            map.put("valueText", first.getValueText());
            map.put("valueNumber", first.getValueNumber());
            map.put("unit", first.getUnit() == null ? definition.getDefaultUnit() : first.getUnit());
            map.put("selectedOptions", group.stream().map(ProductAttributeValue::getOption).filter(Objects::nonNull).map(this::optionMap).toList());
            map.put("displayOrder", first.getDisplayOrder());
            return map;
        }).toList();
    }

    private Map<String, Object> variantMap(ProductVariant variant) {
        Map<String, Object> map = linkedMap();
        map.put("id", variant.getId());
        map.put("sku", variant.getSku());
        map.put("combinationKey", variant.getCombinationKey());
        map.put("salePrice", variant.getSalePrice());
        map.put("quantity", variant.getQuantity());
        map.put("imageUrl", variant.getImageUrl());
        map.put("isDefault", variant.isDefaultVariant());
        map.put("status", variant.getStatus());
        map.put("selections", selections(variant.getId()));
        return map;
    }

    private CatalogVariantSnapshot snapshot(ProductVariant variant) {
        List<VariantSelection> selections = variantSelections(variant.getId());
        String label = selections.isEmpty() ? "Mặc định" : selections.stream()
                .map(item -> item.axisName() + ": " + item.value()).collect(Collectors.joining(" · "));
        return new CatalogVariantSnapshot(
                variant.getId(), variant.getProduct().getId(), variant.getProduct().getCategory().getId(), variant.getProduct().getSellerId(),
                variant.getSku(), variant.getProduct().getName(), label, selections, variant.getSalePrice(),
                variant.getQuantity(), variant.getImageUrl(), variant.getStatus().name());
    }

    private List<VariantSelection> variantSelections(String variantId) {
        return mappingRepository.findByVariant_Id(variantId).stream()
                .sorted(Comparator.comparing(mapping -> mapping.getAxisValue().getAxis().getDisplayOrder()))
                .map(mapping -> {
                    ProductVariantAxisValue value = mapping.getAxisValue();
                    return new VariantSelection(value.getAxis().getId(), value.getAxis().getName(), value.getId(), value.getValue());
                }).toList();
    }

    private List<Map<String, Object>> selections(String variantId) {
        return mappingRepository.findByVariant_Id(variantId).stream()
                .sorted(Comparator.comparing(mapping -> mapping.getAxisValue().getAxis().getDisplayOrder()))
                .map(mapping -> {
                    ProductVariantAxisValue value = mapping.getAxisValue();
                    Map<String, Object> item = linkedMap();
                    item.put("axisId", value.getAxis().getId());
                    item.put("axisName", value.getAxis().getName());
                    item.put("valueId", value.getId());
                    item.put("value", value.getValue());
                    return item;
                }).toList();
    }

    private Map<String, Object> axisMap(ProductVariantAxis axis) {
        Map<String, Object> map = linkedMap();
        map.put("id", axis.getId());
        map.put("name", axis.getName());
        map.put("normalizedName", axis.getNormalizedName());
        map.put("nameSuggestionId", axis.getNameSuggestion() == null ? null : axis.getNameSuggestion().getId());
        map.put("displayOrder", axis.getDisplayOrder());
        map.put("values", axisValueRepository.findByAxis_IdAndStatusOrderByDisplayOrderAsc(axis.getId(), EntityStatus.ACTIVE)
                .stream().map(value -> {
                    Map<String, Object> item = linkedMap();
                    item.put("id", value.getId());
                    item.put("value", value.getValue());
                    item.put("normalizedValue", value.getNormalizedValue());
                    item.put("displayOrder", value.getDisplayOrder());
                    return item;
                }).toList());
        return map;
    }

    private Map<String, Object> definitionMap(ProductAttributeDefinition definition, CategoryAttributeSuggestion link) {
        Map<String, Object> map = linkedMap();
        map.put("definitionId", definition.getId());
        map.put("name", definition.getName());
        map.put("dataType", definition.getDataType());
        map.put("defaultUnit", definition.getDefaultUnit());
        map.put("verified", definition.isVerified());
        map.put("required", link.isRequiredValue());
        map.put("filterable", link.isFilterable());
        map.put("displayOrder", link.getDisplayOrder());
        map.put("options", optionRepository.findByDefinition_IdAndStatusOrderByDisplayOrderAsc(definition.getId(), EntityStatus.ACTIVE)
                .stream().map(this::optionMap).toList());
        return map;
    }

    private Map<String, Object> optionMap(ProductAttributeOption option) {
        Map<String, Object> map = linkedMap();
        map.put("id", option.getId());
        map.put("value", option.getValue());
        map.put("verified", option.isVerified());
        map.put("resolvedOptionId", option.getMergedIntoOptionId() == null ? option.getId() : option.getMergedIntoOptionId());
        return map;
    }

    private Map<String, Object> categoryNode(Category category) {
        Map<String, Object> map = categoryRef(category);
        map.put("children", categoryRepository.findByParent_IdAndStatusOrderByDisplayOrderAsc(category.getId(), EntityStatus.ACTIVE)
                .stream().map(this::categoryNode).toList());
        return map;
    }

    private Map<String, Object> categoryRef(Category category) {
        Map<String, Object> map = linkedMap();
        map.put("id", category.getId());
        map.put("code", category.getCode());
        map.put("name", category.getName());
        map.put("slug", category.getSlug());
        return map;
    }

    private Map<String, Object> imageMap(ProductImage image) {
        Map<String, Object> map = linkedMap();
        map.put("id", image.getId());
        map.put("url", image.getUrl());
        map.put("displayOrder", image.getDisplayOrder());
        map.put("status", image.getStatus());
        return map;
    }

    private Map<String, Object> axisSuggestionMap(VariantAxisNameSuggestion suggestion) {
        Map<String, Object> map = linkedMap();
        map.put("id", suggestion.getId());
        map.put("name", suggestion.getName());
        map.put("verified", suggestion.isVerified());
        map.put("resolvedSuggestionId", suggestion.getMergedIntoSuggestionId() == null ? suggestion.getId() : suggestion.getMergedIntoSuggestionId());
        return map;
    }

    private void enqueue(String productId, String eventType, Map<String, Object> payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Product");
        event.setAggregateId(productId);
        event.setEventType(eventType);
        if (payload != null) {
            try {
                event.setPayload(objectMapper.writeValueAsString(payload));
            } catch (JsonProcessingException exception) {
                throw new IllegalStateException("Cannot serialize product outbox payload", exception);
            }
        }
        outboxRepository.save(event);
    }

    private static PageRequest page(ProductSearchRequest request) {
        int page = Math.max(0, request.getPage());
        int size = request.getSize() <= 0 ? 20 : Math.min(100, request.getSize());
        return PageRequest.of(page, size);
    }

    private static String safeQuery(String q) { return q == null ? "" : q.trim(); }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static void requireSeller(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) throw new SecurityException("SELLER_ID_REQUIRED");
    }
    private static <T> List<T> safe(List<T> values) { return values == null ? List.of() : values; }
    private static Map<String, Object> linkedMap() { return new LinkedHashMap<>(); }

    private record AttributeCriterion(List<String> values, BigDecimal min, BigDecimal max) {}
}
