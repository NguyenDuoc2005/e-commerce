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
import com.ecommerce.catalog.entity.ProductAttributeValueOption;
import com.ecommerce.catalog.model.request.DynamicAttributeValueRequest;
import com.ecommerce.catalog.model.request.DynamicAttributeFilterSelection;
import com.ecommerce.catalog.model.response.DynamicAttributeOptionResponse;
import com.ecommerce.catalog.model.response.DynamicAttributeResponse;
import com.ecommerce.catalog.model.response.DynamicFilterResponse;
import com.ecommerce.catalog.repository.CategoryAttributeSuggestionRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductAttributeDefinitionRepository;
import com.ecommerce.catalog.repository.ProductAttributeOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DynamicAttributeService {

    public static final int MAX_ATTRIBUTES_PER_PRODUCT = 50;
    private static final int MAX_OPTIONS_PER_ATTRIBUTE = 100;

    private final ProductAttributeDefinitionRepository definitionRepository;
    private final ProductAttributeOptionRepository optionRepository;
    private final CategoryAttributeSuggestionRepository suggestionRepository;
    private final ProductAttributeValueRepository valueRepository;
    private final ProductAttributeValueOptionRepository valueOptionRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public DynamicAttributeService(
            ProductAttributeDefinitionRepository definitionRepository,
            ProductAttributeOptionRepository optionRepository,
            CategoryAttributeSuggestionRepository suggestionRepository,
            ProductAttributeValueRepository valueRepository,
            ProductAttributeValueOptionRepository valueOptionRepository,
            CategoryRepository categoryRepository,
            ObjectMapper objectMapper
    ) {
        this.definitionRepository = definitionRepository;
        this.optionRepository = optionRepository;
        this.suggestionRepository = suggestionRepository;
        this.valueRepository = valueRepository;
        this.valueOptionRepository = valueOptionRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DynamicAttributeResponse> suggestions(String categoryId, String sellerId, String query) {
        requireText(categoryId, "Danh muc khong hop le");
        requireText(sellerId, "SellerId khong hop le");
        String normalizedQuery = normalize(query);
        return visibleSuggestions(categoryId, sellerId).stream()
                .filter(item -> !StringUtils.hasText(normalizedQuery)
                        || item.getAttribute().getNormalizedName().contains(normalizedQuery))
                .map(item -> response(item.getAttribute(), item, null, sellerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DynamicAttributeResponse> productValues(String productId, String sellerId) {
        return valueRepository.findByProduct_IdOrderByDisplayOrderAsc(productId).stream()
                .filter(value -> value.getAttribute().getStatus() == EntityStatus.ACTIVE)
                .filter(value -> value.getAttribute().getNormalizationStatus() != AttributeNormalizationStatus.HIDDEN)
                .map(value -> response(value.getAttribute(), null, value, sellerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DynamicFilterResponse> publicFilters(String categoryId) {
        requireText(categoryId, "Danh muc khong hop le");
        return suggestionRepository.findByCategory_IdAndStatusOrderByDisplayOrderAsc(categoryId, EntityStatus.ACTIVE).stream()
                .filter(CategoryAttributeSuggestion::isFilterable)
                .filter(item -> item.getAttribute().getStatus() == EntityStatus.ACTIVE)
                .filter(item -> item.getAttribute().getNormalizationStatus() == AttributeNormalizationStatus.STANDARDIZED)
                .map(item -> filterResponse(item, categoryId))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean matchesProductFilters(String productId, String filtersJson) {
        if (!StringUtils.hasText(filtersJson)) {
            return true;
        }
        Map<String, DynamicAttributeFilterSelection> filters;
        try {
            filters = objectMapper.readValue(
                    filtersJson,
                    new TypeReference<Map<String, DynamicAttributeFilterSelection>>() { }
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Bo loc thuoc tinh khong dung dinh dang JSON", ex);
        }
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        Map<String, ProductAttributeValue> values = valueRepository.findByProduct_IdOrderByDisplayOrderAsc(productId).stream()
                .collect(Collectors.toMap(value -> value.getAttribute().getId(), Function.identity()));
        return filters.entrySet().stream().allMatch(entry -> matches(values.get(entry.getKey()), entry.getValue()));
    }

    @Transactional
    public void replaceProductAttributes(Product product, String sellerId, String attributesJson) {
        if (attributesJson == null) {
            return;
        }
        requireText(sellerId, "SellerId khong hop le");
        List<DynamicAttributeValueRequest> requests = parse(attributesJson);
        if (requests.size() > MAX_ATTRIBUTES_PER_PRODUCT) {
            throw new IllegalArgumentException("Moi san pham chi duoc toi da 50 thuoc tinh");
        }
        if (!requests.isEmpty() && product.getCategory() == null) {
            throw new IllegalArgumentException("Phai chon danh muc truoc khi nhap thuoc tinh");
        }

        Set<String> uniqueAttributes = new LinkedHashSet<>();
        List<ResolvedAttribute> resolved = new ArrayList<>();
        for (int index = 0; index < requests.size(); index++) {
            DynamicAttributeValueRequest request = requests.get(index);
            ProductAttributeDefinition definition = resolveDefinition(product.getCategory(), sellerId, request, index);
            if (!uniqueAttributes.add(definition.getId())) {
                throw new IllegalArgumentException("Khong duoc nhap trung thuoc tinh " + definition.getName());
            }
            resolved.add(new ResolvedAttribute(definition, request, resolveSelectedOptions(definition, sellerId, request)));
        }

        List<ProductAttributeValue> oldValues = valueRepository.findByProduct_IdOrderByDisplayOrderAsc(product.getId());
        if (!oldValues.isEmpty()) {
            valueOptionRepository.deleteByValueIds(oldValues.stream().map(ProductAttributeValue::getId).toList());
            valueRepository.deleteAll(oldValues);
            valueRepository.flush();
        }

        for (int index = 0; index < resolved.size(); index++) {
            saveValue(product, resolved.get(index), index);
        }
    }

    private List<DynamicAttributeValueRequest> parse(String attributesJson) {
        if (!StringUtils.hasText(attributesJson)) {
            return List.of();
        }
        try {
            List<DynamicAttributeValueRequest> values = objectMapper.readValue(
                    attributesJson,
                    new TypeReference<List<DynamicAttributeValueRequest>>() { }
            );
            return values == null ? List.of() : values;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Danh sach thuoc tinh khong dung dinh dang JSON", ex);
        }
    }

    private ProductAttributeDefinition resolveDefinition(
            Category category,
            String sellerId,
            DynamicAttributeValueRequest request,
            int index
    ) {
        if (StringUtils.hasText(request.getAttributeId())) {
            ProductAttributeDefinition definition = definitionRepository.findById(request.getAttributeId())
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thuoc tinh"));
            if (!isVisibleToSeller(definition, sellerId)
                    || !suggestionRepository.existsByCategory_IdAndAttribute_IdAndStatus(
                    category.getId(), definition.getId(), EntityStatus.ACTIVE)) {
                throw new IllegalArgumentException("Thuoc tinh khong thuoc danh muc hoac khong thuoc shop");
            }
            if (request.getDataType() != null && request.getDataType() != definition.getDataType()) {
                throw new IllegalArgumentException("Khong duoc doi kieu cua thuoc tinh da ton tai");
            }
            validateTypedValue(definition.getDataType(), request);
            return definition;
        }

        requireText(request.getName(), "Ten thuoc tinh khong duoc de trong");
        if (request.getName().trim().length() > 255) {
            throw new IllegalArgumentException("Ten thuoc tinh khong duoc qua 255 ky tu");
        }
        if (request.getDataType() == null) {
            throw new IllegalArgumentException("Phai chon kieu du lieu cho thuoc tinh moi");
        }
        validateTypedValue(request.getDataType(), request);

        String normalizedName = normalize(request.getName());
        ProductAttributeDefinition existing = visibleSuggestions(category.getId(), sellerId).stream()
                .map(CategoryAttributeSuggestion::getAttribute)
                .filter(item -> item.getNormalizedName().equals(normalizedName))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            if (existing.getDataType() != request.getDataType()) {
                throw new IllegalArgumentException("Thuoc tinh trung ten da ton tai voi kieu " + existing.getDataType());
            }
            return existing;
        }

        ProductAttributeDefinition definition = new ProductAttributeDefinition();
        definition.setCode("ATTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        definition.setName(request.getName().trim());
        definition.setNormalizedName(normalizedName);
        definition.setDataType(request.getDataType());
        definition.setCreatorSellerId(sellerId);
        definition.setNormalizationStatus(AttributeNormalizationStatus.PENDING);
        definition.setStatus(EntityStatus.ACTIVE);
        definitionRepository.save(definition);

        CategoryAttributeSuggestion suggestion = new CategoryAttributeSuggestion();
        suggestion.setCategory(category);
        suggestion.setAttribute(definition);
        suggestion.setDefaultSuggestion(true);
        suggestion.setFilterable(false);
        suggestion.setRequiredValue(false);
        suggestion.setDisplayOrder(index);
        suggestion.setStatus(EntityStatus.ACTIVE);
        suggestionRepository.save(suggestion);
        return definition;
    }

    private List<ProductAttributeOption> resolveSelectedOptions(
            ProductAttributeDefinition definition,
            String sellerId,
            DynamicAttributeValueRequest request
    ) {
        if (definition.getDataType() != AttributeDataType.SINGLE_SELECT
                && definition.getDataType() != AttributeDataType.MULTI_SELECT) {
            return List.of();
        }

        List<ProductAttributeOption> existingOptions = optionRepository
                .findByAttribute_IdAndStatusOrderByDisplayOrderAsc(definition.getId(), EntityStatus.ACTIVE);
        Map<String, ProductAttributeOption> byId = existingOptions.stream()
                .collect(Collectors.toMap(ProductAttributeOption::getId, Function.identity()));
        Map<String, ProductAttributeOption> byNormalizedValue = existingOptions.stream()
                .collect(Collectors.toMap(ProductAttributeOption::getNormalizedValue, Function.identity(), (left, right) -> left));

        if (Objects.equals(definition.getCreatorSellerId(), sellerId)
                && definition.getNormalizationStatus() == AttributeNormalizationStatus.PENDING) {
            List<String> optionValues = uniqueNonBlank(request.getOptionValues());
            if (optionValues.size() > MAX_OPTIONS_PER_ATTRIBUTE) {
                throw new IllegalArgumentException("Moi thuoc tinh dropdown chi duoc toi da 100 lua chon");
            }
            for (String rawValue : optionValues) {
                String normalizedValue = normalize(rawValue);
                if (!byNormalizedValue.containsKey(normalizedValue)) {
                    ProductAttributeOption option = new ProductAttributeOption();
                    option.setAttribute(definition);
                    option.setValue(rawValue.trim());
                    option.setNormalizedValue(normalizedValue);
                    option.setCreatorSellerId(sellerId);
                    option.setDisplayOrder(byNormalizedValue.size());
                    option.setStatus(EntityStatus.ACTIVE);
                    optionRepository.save(option);
                    byId.put(option.getId(), option);
                    byNormalizedValue.put(normalizedValue, option);
                }
            }
        }

        LinkedHashSet<ProductAttributeOption> selected = new LinkedHashSet<>();
        for (String optionId : request.getSelectedOptionIds()) {
            ProductAttributeOption option = byId.get(optionId);
            if (option == null) {
                throw new IllegalArgumentException("Lua chon dropdown khong thuoc thuoc tinh");
            }
            selected.add(option);
        }
        for (String selectedValue : uniqueNonBlank(request.getSelectedOptionValues())) {
            ProductAttributeOption option = byNormalizedValue.get(normalize(selectedValue));
            if (option == null) {
                throw new IllegalArgumentException("Gia tri dropdown chua nam trong danh sach lua chon");
            }
            selected.add(option);
        }

        if (definition.getDataType() == AttributeDataType.SINGLE_SELECT && selected.size() != 1) {
            throw new IllegalArgumentException("Thuoc tinh chon mot phai co dung mot gia tri");
        }
        if (definition.getDataType() == AttributeDataType.MULTI_SELECT && selected.isEmpty()) {
            throw new IllegalArgumentException("Thuoc tinh chon nhieu phai co it nhat mot gia tri");
        }
        return List.copyOf(selected);
    }

    private void validateTypedValue(AttributeDataType dataType, DynamicAttributeValueRequest request) {
        switch (dataType) {
            case TEXT -> {
                requireText(request.getTextValue(), "Gia tri text khong duoc de trong");
                if (request.getTextValue().length() > 5000) {
                    throw new IllegalArgumentException("Gia tri text khong duoc qua 5000 ky tu");
                }
            }
            case NUMBER -> {
                if (request.getNumberValue() == null) {
                    throw new IllegalArgumentException("Gia tri number khong duoc de trong");
                }
                if (request.getUnit() != null && request.getUnit().length() > 50) {
                    throw new IllegalArgumentException("Don vi khong duoc qua 50 ky tu");
                }
            }
            case SINGLE_SELECT, MULTI_SELECT -> {
                if (request.getSelectedOptionIds().isEmpty() && request.getSelectedOptionValues().isEmpty()) {
                    throw new IllegalArgumentException("Phai chon gia tri dropdown");
                }
            }
        }
    }

    private void saveValue(Product product, ResolvedAttribute resolved, int index) {
        ProductAttributeValue value = new ProductAttributeValue();
        value.setProduct(product);
        value.setAttribute(resolved.definition());
        value.setDisplayOrder(resolved.request().getDisplayOrder() == null ? index : resolved.request().getDisplayOrder());
        value.setStatus(EntityStatus.ACTIVE);
        if (resolved.definition().getDataType() == AttributeDataType.TEXT) {
            value.setTextValue(resolved.request().getTextValue().trim());
        } else if (resolved.definition().getDataType() == AttributeDataType.NUMBER) {
            value.setNumberValue(resolved.request().getNumberValue());
            value.setUnit(trimToNull(resolved.request().getUnit()));
        }
        valueRepository.save(value);

        for (ProductAttributeOption option : resolved.selectedOptions()) {
            ProductAttributeValueOption selected = new ProductAttributeValueOption();
            selected.setProductAttributeValue(value);
            selected.setOption(option);
            valueOptionRepository.save(selected);
        }
    }

    private DynamicAttributeResponse response(
            ProductAttributeDefinition definition,
            CategoryAttributeSuggestion suggestion,
            ProductAttributeValue value,
            String sellerId
    ) {
        List<DynamicAttributeOptionResponse> options = optionRepository
                .findByAttribute_IdAndStatusOrderByDisplayOrderAsc(definition.getId(), EntityStatus.ACTIVE).stream()
                .map(option -> new DynamicAttributeOptionResponse(option.getId(), option.getValue()))
                .toList();
        List<String> selectedOptionIds = value == null ? List.of() : valueOptionRepository
                .findByProductAttributeValue_Id(value.getId()).stream()
                .map(item -> item.getOption().getId())
                .toList();
        return new DynamicAttributeResponse(
                definition.getId(),
                definition.getName(),
                definition.getDataType(),
                definition.getNormalizationStatus(),
                Objects.equals(definition.getCreatorSellerId(), sellerId),
                suggestion != null && suggestion.isDefaultSuggestion(),
                suggestion != null && suggestion.isFilterable(),
                suggestion != null && suggestion.isRequiredValue(),
                options,
                value == null ? null : value.getTextValue(),
                value == null ? null : value.getNumberValue(),
                value == null ? null : value.getUnit(),
                selectedOptionIds,
                value == null ? suggestion == null ? 0 : suggestion.getDisplayOrder() : value.getDisplayOrder()
        );
    }

    private DynamicFilterResponse filterResponse(CategoryAttributeSuggestion suggestion, String categoryId) {
        ProductAttributeDefinition definition = suggestion.getAttribute();
        List<ProductAttributeValue> values = valueRepository
                .findByProduct_Category_IdAndAttribute_IdAndStatus(categoryId, definition.getId(), EntityStatus.ACTIVE);
        List<DynamicAttributeOptionResponse> options;
        java.math.BigDecimal min = null;
        java.math.BigDecimal max = null;
        if (definition.getDataType() == AttributeDataType.TEXT) {
            Map<String, String> unique = new LinkedHashMap<>();
            values.stream().map(ProductAttributeValue::getTextValue).filter(StringUtils::hasText)
                    .forEach(value -> unique.putIfAbsent(normalize(value), value));
            options = unique.entrySet().stream()
                    .map(entry -> new DynamicAttributeOptionResponse(entry.getKey(), entry.getValue()))
                    .toList();
        } else if (definition.getDataType() == AttributeDataType.NUMBER) {
            List<java.math.BigDecimal> numbers = values.stream()
                    .map(ProductAttributeValue::getNumberValue)
                    .filter(Objects::nonNull)
                    .toList();
            min = numbers.stream().min(java.math.BigDecimal::compareTo).orElse(null);
            max = numbers.stream().max(java.math.BigDecimal::compareTo).orElse(null);
            options = List.of();
        } else {
            Set<String> usedOptionIds = values.stream()
                    .flatMap(value -> valueOptionRepository.findByProductAttributeValue_Id(value.getId()).stream())
                    .map(item -> item.getOption().getId())
                    .collect(Collectors.toSet());
            options = optionRepository.findByAttribute_IdAndStatusOrderByDisplayOrderAsc(definition.getId(), EntityStatus.ACTIVE).stream()
                    .filter(option -> usedOptionIds.contains(option.getId()))
                    .map(option -> new DynamicAttributeOptionResponse(option.getId(), option.getValue()))
                    .toList();
        }
        return new DynamicFilterResponse(
                definition.getId(), definition.getName(), definition.getDataType(), options,
                min, max, suggestion.getDisplayOrder()
        );
    }

    private boolean matches(ProductAttributeValue value, DynamicAttributeFilterSelection filter) {
        if (value == null) {
            return false;
        }
        AttributeDataType type = value.getAttribute().getDataType();
        if (type == AttributeDataType.TEXT) {
            if (filter.getValues().isEmpty()) return true;
            String normalizedValue = normalize(value.getTextValue());
            return filter.getValues().stream().map(DynamicAttributeService::normalize).anyMatch(normalizedValue::equals);
        }
        if (type == AttributeDataType.NUMBER) {
            if (value.getNumberValue() == null) return false;
            return (filter.getMin() == null || value.getNumberValue().compareTo(filter.getMin()) >= 0)
                    && (filter.getMax() == null || value.getNumberValue().compareTo(filter.getMax()) <= 0);
        }
        if (filter.getValues().isEmpty()) return true;
        Set<String> selectedIds = valueOptionRepository.findByProductAttributeValue_Id(value.getId()).stream()
                .map(item -> item.getOption().getId())
                .collect(Collectors.toSet());
        return filter.getValues().stream().anyMatch(selectedIds::contains);
    }

    private List<CategoryAttributeSuggestion> visibleSuggestions(String categoryId, String sellerId) {
        return suggestionRepository.findByCategory_IdAndStatusOrderByDisplayOrderAsc(categoryId, EntityStatus.ACTIVE).stream()
                .filter(item -> isVisibleToSeller(item.getAttribute(), sellerId))
                .sorted(Comparator.comparing(CategoryAttributeSuggestion::getDisplayOrder))
                .toList();
    }

    private boolean isVisibleToSeller(ProductAttributeDefinition definition, String sellerId) {
        if (definition.getStatus() != EntityStatus.ACTIVE) {
            return false;
        }
        if (definition.getNormalizationStatus() == AttributeNormalizationStatus.STANDARDIZED) {
            return true;
        }
        return definition.getNormalizationStatus() == AttributeNormalizationStatus.PENDING
                && Objects.equals(definition.getCreatorSellerId(), sellerId);
    }

    private List<String> uniqueNonBlank(List<String> values) {
        if (values == null) {
            return List.of();
        }
        Map<String, String> unique = new LinkedHashMap<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                unique.putIfAbsent(normalize(value), value.trim());
            }
        }
        return new ArrayList<>(unique.values());
    }

    static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replaceAll("\\s+", " ");
        return normalized;
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record ResolvedAttribute(
            ProductAttributeDefinition definition,
            DynamicAttributeValueRequest request,
            List<ProductAttributeOption> selectedOptions
    ) {
    }
}
