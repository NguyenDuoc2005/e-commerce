package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.AttributeNormalizationStatus;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import com.ecommerce.catalog.entity.ProductAttributeModerationAudit;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import com.ecommerce.catalog.entity.ProductAttributeValue;
import com.ecommerce.catalog.entity.ProductAttributeValueOption;
import com.ecommerce.catalog.model.request.AttributeMergeRequest;
import com.ecommerce.catalog.model.request.AttributeStandardizeRequest;
import com.ecommerce.catalog.model.response.AdminAttributeResponse;
import com.ecommerce.catalog.model.response.DynamicAttributeOptionResponse;
import com.ecommerce.catalog.repository.CategoryAttributeSuggestionRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductAttributeDefinitionRepository;
import com.ecommerce.catalog.repository.ProductAttributeModerationAuditRepository;
import com.ecommerce.catalog.repository.ProductAttributeOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueRepository;
import com.ecommerce.catalog.service.impl.ProductOutboxServiceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminDynamicAttributeService {

    private final ProductAttributeDefinitionRepository definitionRepository;
    private final ProductAttributeOptionRepository optionRepository;
    private final CategoryAttributeSuggestionRepository suggestionRepository;
    private final ProductAttributeValueRepository valueRepository;
    private final ProductAttributeValueOptionRepository valueOptionRepository;
    private final ProductAttributeModerationAuditRepository auditRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOutboxService productOutboxService;

    public AdminDynamicAttributeService(
            ProductAttributeDefinitionRepository definitionRepository,
            ProductAttributeOptionRepository optionRepository,
            CategoryAttributeSuggestionRepository suggestionRepository,
            ProductAttributeValueRepository valueRepository,
            ProductAttributeValueOptionRepository valueOptionRepository,
            ProductAttributeModerationAuditRepository auditRepository,
            CategoryRepository categoryRepository,
            ProductOutboxService productOutboxService
    ) {
        this.definitionRepository = definitionRepository;
        this.optionRepository = optionRepository;
        this.suggestionRepository = suggestionRepository;
        this.valueRepository = valueRepository;
        this.valueOptionRepository = valueOptionRepository;
        this.auditRepository = auditRepository;
        this.categoryRepository = categoryRepository;
        this.productOutboxService = productOutboxService;
    }

    @Transactional(readOnly = true)
    public List<AdminAttributeResponse> list(String query, AttributeNormalizationStatus status) {
        String normalizedQuery = DynamicAttributeService.normalize(query);
        return definitionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate")).stream()
                .filter(item -> status == null || item.getNormalizationStatus() == status)
                .filter(item -> !StringUtils.hasText(normalizedQuery) || item.getNormalizedName().contains(normalizedQuery))
                .map(this::response)
                .toList();
    }

    @Transactional
    public AdminAttributeResponse standardize(String attributeId, AttributeStandardizeRequest request, String actorUserId) {
        ProductAttributeDefinition definition = activeDefinition(attributeId);
        if (StringUtils.hasText(request.getName())) {
            definition.setName(request.getName().trim());
            definition.setNormalizedName(DynamicAttributeService.normalize(request.getName()));
        }
        definition.setNormalizationStatus(AttributeNormalizationStatus.STANDARDIZED);
        definition.setStatus(EntityStatus.ACTIVE);
        definitionRepository.save(definition);

        List<CategoryAttributeSuggestion> existing = suggestionRepository.findByAttribute_IdOrderByDisplayOrderAsc(attributeId);
        existing.forEach(item -> {
            item.setStatus(EntityStatus.ACTIVE);
            item.setFilterable(request.isFilterable());
            suggestionRepository.save(item);
        });
        for (String categoryId : request.getCategoryIds()) {
            suggestionRepository.findByCategory_IdAndAttribute_Id(categoryId, attributeId).ifPresentOrElse(item -> {
                item.setStatus(EntityStatus.ACTIVE);
                item.setDefaultSuggestion(true);
                item.setFilterable(request.isFilterable());
                suggestionRepository.save(item);
            }, () -> categoryRepository.findById(categoryId).ifPresent(category -> {
                CategoryAttributeSuggestion item = new CategoryAttributeSuggestion();
                item.setCategory(category);
                item.setAttribute(definition);
                item.setDefaultSuggestion(true);
                item.setFilterable(request.isFilterable());
                item.setRequiredValue(false);
                item.setDisplayOrder(existing.size());
                item.setStatus(EntityStatus.ACTIVE);
                suggestionRepository.save(item);
            }));
        }

        Set<String> affectedProducts = productIds(attributeId);
        publishChanged(affectedProducts);
        audit("STANDARDIZE", actorUserId, attributeId, null, request.getReason(), affectedProducts.size());
        return response(definition);
    }

    @Transactional
    public AdminAttributeResponse merge(String sourceAttributeId, AttributeMergeRequest request, String actorUserId) {
        if (request == null || !StringUtils.hasText(request.getTargetAttributeId())) {
            throw new IllegalArgumentException("Phai chon thuoc tinh dich de gop");
        }
        if (sourceAttributeId.equals(request.getTargetAttributeId())) {
            throw new IllegalArgumentException("Khong the gop thuoc tinh vao chinh no");
        }
        ProductAttributeDefinition source = activeDefinition(sourceAttributeId);
        ProductAttributeDefinition target = activeDefinition(request.getTargetAttributeId());
        if (source.getDataType() != target.getDataType()) {
            throw new IllegalArgumentException("Chi duoc gop hai thuoc tinh cung kieu du lieu");
        }

        Map<String, ProductAttributeOption> targetOptions = new LinkedHashMap<>();
        optionRepository.findByAttribute_IdAndStatusOrderByDisplayOrderAsc(target.getId(), EntityStatus.ACTIVE)
                .forEach(option -> targetOptions.put(option.getNormalizedValue(), option));

        Set<String> affectedProducts = new LinkedHashSet<>();
        for (ProductAttributeValue sourceValue : new ArrayList<>(valueRepository.findByAttribute_Id(source.getId()))) {
            String productId = sourceValue.getProduct().getId();
            affectedProducts.add(productId);
            ProductAttributeValue targetValue = valueRepository.findByProduct_IdAndAttribute_Id(productId, target.getId())
                    .orElseGet(() -> copyValue(sourceValue, target));
            mergeSelectedOptions(sourceValue, targetValue, target, targetOptions);
            valueOptionRepository.deleteByValueIds(List.of(sourceValue.getId()));
            valueRepository.delete(sourceValue);
        }

        for (CategoryAttributeSuggestion sourceLink : suggestionRepository.findByAttribute_IdOrderByDisplayOrderAsc(source.getId())) {
            suggestionRepository.findByCategory_IdAndAttribute_Id(sourceLink.getCategory().getId(), target.getId())
                    .ifPresentOrElse(targetLink -> {
                        targetLink.setDefaultSuggestion(targetLink.isDefaultSuggestion() || sourceLink.isDefaultSuggestion());
                        targetLink.setFilterable(targetLink.isFilterable() || sourceLink.isFilterable());
                        targetLink.setStatus(EntityStatus.ACTIVE);
                        suggestionRepository.save(targetLink);
                    }, () -> {
                        CategoryAttributeSuggestion targetLink = new CategoryAttributeSuggestion();
                        targetLink.setCategory(sourceLink.getCategory());
                        targetLink.setAttribute(target);
                        targetLink.setDefaultSuggestion(sourceLink.isDefaultSuggestion());
                        targetLink.setFilterable(sourceLink.isFilterable());
                        targetLink.setRequiredValue(sourceLink.isRequiredValue());
                        targetLink.setDisplayOrder(sourceLink.getDisplayOrder());
                        targetLink.setStatus(EntityStatus.ACTIVE);
                        suggestionRepository.save(targetLink);
                    });
            sourceLink.setStatus(EntityStatus.INACTIVE);
            suggestionRepository.save(sourceLink);
        }

        source.setNormalizationStatus(AttributeNormalizationStatus.MERGED);
        source.setMergedIntoAttributeId(target.getId());
        source.setStatus(EntityStatus.INACTIVE);
        definitionRepository.save(source);
        publishChanged(affectedProducts);
        audit("MERGE", actorUserId, source.getId(), target.getId(), request.getReason(), affectedProducts.size());
        return response(target);
    }

    @Transactional
    public void hide(String attributeId, String reason, String actorUserId) {
        ProductAttributeDefinition definition = activeDefinition(attributeId);
        Set<String> affectedProducts = productIds(attributeId);
        definition.setNormalizationStatus(AttributeNormalizationStatus.HIDDEN);
        definition.setStatus(EntityStatus.INACTIVE);
        definitionRepository.save(definition);
        suggestionRepository.findByAttribute_IdOrderByDisplayOrderAsc(attributeId).forEach(item -> {
            item.setStatus(EntityStatus.INACTIVE);
            suggestionRepository.save(item);
        });
        publishChanged(affectedProducts);
        audit("HIDE", actorUserId, attributeId, null, reason, affectedProducts.size());
    }

    private ProductAttributeDefinition activeDefinition(String id) {
        ProductAttributeDefinition definition = definitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thuoc tinh"));
        if (definition.getNormalizationStatus() == AttributeNormalizationStatus.MERGED
                || definition.getNormalizationStatus() == AttributeNormalizationStatus.HIDDEN) {
            throw new IllegalArgumentException("Thuoc tinh khong con o trang thai co the thao tac");
        }
        return definition;
    }

    private ProductAttributeValue copyValue(ProductAttributeValue source, ProductAttributeDefinition target) {
        ProductAttributeValue value = new ProductAttributeValue();
        value.setProduct(source.getProduct());
        value.setAttribute(target);
        value.setTextValue(source.getTextValue());
        value.setNumberValue(source.getNumberValue());
        value.setUnit(source.getUnit());
        value.setDisplayOrder(source.getDisplayOrder());
        value.setStatus(EntityStatus.ACTIVE);
        return valueRepository.save(value);
    }

    private void mergeSelectedOptions(
            ProductAttributeValue sourceValue,
            ProductAttributeValue targetValue,
            ProductAttributeDefinition target,
            Map<String, ProductAttributeOption> targetOptions
    ) {
        if (sourceValue.getAttribute().getDataType().name().endsWith("SELECT")) {
            Set<String> alreadySelected = valueOptionRepository.findByProductAttributeValue_Id(targetValue.getId()).stream()
                    .map(item -> item.getOption().getId())
                    .collect(java.util.stream.Collectors.toSet());
            if (sourceValue.getAttribute().getDataType().name().equals("SINGLE_SELECT") && !alreadySelected.isEmpty()) {
                return;
            }
            for (ProductAttributeValueOption sourceSelection : valueOptionRepository.findByProductAttributeValue_Id(sourceValue.getId())) {
                ProductAttributeOption sourceOption = sourceSelection.getOption();
                ProductAttributeOption targetOption = targetOptions.computeIfAbsent(sourceOption.getNormalizedValue(), key -> {
                    ProductAttributeOption option = new ProductAttributeOption();
                    option.setAttribute(target);
                    option.setValue(sourceOption.getValue());
                    option.setNormalizedValue(sourceOption.getNormalizedValue());
                    option.setCreatorSellerId(null);
                    option.setDisplayOrder(targetOptions.size());
                    option.setStatus(EntityStatus.ACTIVE);
                    return optionRepository.save(option);
                });
                if (alreadySelected.add(targetOption.getId())) {
                    ProductAttributeValueOption selection = new ProductAttributeValueOption();
                    selection.setProductAttributeValue(targetValue);
                    selection.setOption(targetOption);
                    valueOptionRepository.save(selection);
                }
            }
        }
    }

    private AdminAttributeResponse response(ProductAttributeDefinition definition) {
        List<DynamicAttributeOptionResponse> options = optionRepository
                .findByAttribute_IdAndStatusOrderByDisplayOrderAsc(definition.getId(), EntityStatus.ACTIVE).stream()
                .map(option -> new DynamicAttributeOptionResponse(option.getId(), option.getValue()))
                .toList();
        List<AdminAttributeResponse.CategoryRef> categories = suggestionRepository
                .findByAttribute_IdOrderByDisplayOrderAsc(definition.getId()).stream()
                .filter(item -> item.getStatus() == EntityStatus.ACTIVE)
                .map(item -> new AdminAttributeResponse.CategoryRef(
                        item.getCategory().getId(), item.getCategory().getName(), item.isFilterable()))
                .toList();
        return new AdminAttributeResponse(
                definition.getId(), definition.getCode(), definition.getName(), definition.getDataType(),
                definition.getNormalizationStatus(), definition.getCreatorSellerId(),
                definition.getMergedIntoAttributeId(), valueRepository.countByAttribute_Id(definition.getId()),
                options, categories
        );
    }

    private Set<String> productIds(String attributeId) {
        return valueRepository.findByAttribute_Id(attributeId).stream()
                .map(value -> value.getProduct().getId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private void publishChanged(Set<String> productIds) {
        productIds.forEach(id -> productOutboxService.publishChanged(id, ProductOutboxServiceImpl.UPDATED));
    }

    private void audit(String action, String actor, String source, String target, String reason, int affectedCount) {
        ProductAttributeModerationAudit audit = new ProductAttributeModerationAudit();
        audit.setAction(action.toUpperCase(Locale.ROOT));
        audit.setActorUserId(actor);
        audit.setSourceAttributeId(source);
        audit.setTargetAttributeId(target);
        audit.setReason(StringUtils.hasText(reason) ? reason.trim() : null);
        audit.setAffectedProductCount(affectedCount);
        audit.setStatus(EntityStatus.ACTIVE);
        auditRepository.save(audit);
    }
}
