package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import com.ecommerce.catalog.entity.ProductAttributeModerationAudit;
import com.ecommerce.catalog.entity.ProductAttributeOption;
import com.ecommerce.catalog.entity.ProductVariantAxis;
import com.ecommerce.catalog.entity.ProductVariantAxisValue;
import com.ecommerce.catalog.entity.VariantAxisNameSuggestion;
import com.ecommerce.catalog.model.request.AttributeMergeRequest;
import com.ecommerce.catalog.model.request.AttributeStandardizeRequest;
import com.ecommerce.catalog.repository.CategoryAttributeSuggestionRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductAttributeDefinitionRepository;
import com.ecommerce.catalog.repository.ProductAttributeModerationAuditRepository;
import com.ecommerce.catalog.repository.ProductAttributeOptionRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueRepository;
import com.ecommerce.catalog.repository.ProductVariantAxisRepository;
import com.ecommerce.catalog.repository.ProductVariantAxisValueRepository;
import com.ecommerce.catalog.repository.VariantAxisNameSuggestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CatalogAdminService {

    private final ProductAttributeDefinitionRepository definitionRepository;
    private final ProductAttributeOptionRepository optionRepository;
    private final ProductAttributeValueRepository valueRepository;
    private final ProductAttributeModerationAuditRepository auditRepository;
    private final CategoryAttributeSuggestionRepository categorySuggestionRepository;
    private final CategoryRepository categoryRepository;
    private final VariantAxisNameSuggestionRepository axisSuggestionRepository;
    private final ProductVariantAxisRepository axisRepository;
    private final ProductVariantAxisValueRepository axisValueRepository;
    private final CatalogProductService productService;

    public CatalogAdminService(
            ProductAttributeDefinitionRepository definitionRepository,
            ProductAttributeOptionRepository optionRepository,
            ProductAttributeValueRepository valueRepository,
            ProductAttributeModerationAuditRepository auditRepository,
            CategoryAttributeSuggestionRepository categorySuggestionRepository,
            CategoryRepository categoryRepository,
            VariantAxisNameSuggestionRepository axisSuggestionRepository,
            ProductVariantAxisRepository axisRepository,
            ProductVariantAxisValueRepository axisValueRepository,
            CatalogProductService productService
    ) {
        this.definitionRepository = definitionRepository;
        this.optionRepository = optionRepository;
        this.valueRepository = valueRepository;
        this.auditRepository = auditRepository;
        this.categorySuggestionRepository = categorySuggestionRepository;
        this.categoryRepository = categoryRepository;
        this.axisSuggestionRepository = axisSuggestionRepository;
        this.axisRepository = axisRepository;
        this.axisValueRepository = axisValueRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> definitions(String q, Boolean verified, EntityStatus status, String categoryId, String creatorSellerId) {
        EntityStatus effectiveStatus = status == null ? EntityStatus.ACTIVE : status;
        String normalized = ProductAggregateValidator.normalize(q);
        return definitionRepository.findByStatusOrderByVerifiedDescNameAsc(effectiveStatus).stream()
                .filter(definition -> normalized.isBlank() || definition.getNormalizedName().contains(normalized))
                .filter(definition -> verified == null || definition.isVerified() == verified)
                .filter(definition -> creatorSellerId == null || creatorSellerId.equals(definition.getCreatedBySellerId()))
                .filter(definition -> categoryId == null || categorySuggestionRepository
                        .findByCategory_IdAndDefinition_Id(categoryId, definition.getId()).isPresent())
                .map(this::definitionMap).toList();
    }

    @Transactional
    public Map<String, Object> verifyDefinition(String id, String actorUserId) {
        ProductAttributeDefinition definition = definitionRepository.findLockedById(id)
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_DEFINITION_NOT_FOUND"));
        definition.setVerified(true);
        definitionRepository.save(definition);
        audit("VERIFY", actorUserId, id, null, null);
        productService.reindexAll();
        return definitionMap(definition);
    }

    @Transactional
    public Map<String, Object> standardize(String id, AttributeStandardizeRequest request, String actorUserId) {
        ProductAttributeDefinition definition = definitionRepository.findLockedById(id)
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_DEFINITION_NOT_FOUND"));
        if (request.getDefaultUnit() != null && definition.getDataType() != AttributeDataType.NUMBER) {
            throw new IllegalArgumentException("DEFAULT_UNIT_ONLY_FOR_NUMBER");
        }
        definition.setName(request.getName().trim());
        definition.setNormalizedName(ProductAggregateValidator.normalize(request.getName()));
        definition.setDefaultUnit(blankToNull(request.getDefaultUnit()));
        definition.setVerified(true);
        definitionRepository.save(definition);
        List<CategoryAttributeSuggestion> current = categorySuggestionRepository.findByDefinition_IdOrderByDisplayOrderAsc(id);
        categorySuggestionRepository.deleteAll(current);
        categorySuggestionRepository.flush();
        int order = 0;
        List<String> categoryIds = request.getCategoryIds() == null
                ? List.of()
                : new ArrayList<>(new LinkedHashSet<>(request.getCategoryIds()));
        for (String categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("CATEGORY_INVALID"));
            CategoryAttributeSuggestion link = new CategoryAttributeSuggestion();
            link.setCategory(category);
            link.setDefinition(definition);
            link.setDisplayOrder(order++);
            link.setFilterable(true);
            link.setRequiredValue(false);
            link.setStatus(EntityStatus.ACTIVE);
            categorySuggestionRepository.save(link);
        }
        audit("STANDARDIZE", actorUserId, id, null, request.getReason());
        productService.reindexAll();
        return definitionMap(definition);
    }

    @Transactional
    public Map<String, Object> mergeDefinition(String sourceId, AttributeMergeRequest request, String actorUserId) {
        ProductAttributeDefinition source = definitionRepository.findLockedById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_DEFINITION_NOT_FOUND"));
        ProductAttributeDefinition target = definitionRepository.findLockedById(request.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_MERGE_TARGET_NOT_FOUND"));
        if (source.getId().equals(target.getId()) || source.getMergedIntoDefinitionId() != null
                || target.getMergedIntoDefinitionId() != null || source.getDataType() != target.getDataType()) {
            throw new IllegalArgumentException("MERGE_TARGET_MUST_BE_CANONICAL");
        }
        source.setMergedIntoDefinitionId(target.getId());
        source.setStatus(EntityStatus.INACTIVE);
        definitionRepository.save(source);
        audit("MERGE", actorUserId, sourceId, target.getId(), request.getReason());
        productService.reindexAll();
        Map<String, Object> result = definitionMap(source);
        result.put("resolvedDefinitionId", target.getId());
        return result;
    }

    @Transactional
    public void hideDefinition(String id, String reason, String actorUserId) {
        ProductAttributeDefinition definition = definitionRepository.findLockedById(id)
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_DEFINITION_NOT_FOUND"));
        definition.setStatus(EntityStatus.INACTIVE);
        definitionRepository.save(definition);
        audit("HIDE", actorUserId, id, null, reason);
        productService.reindexAll();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> options(String definitionId) {
        return optionRepository.findByDefinition_IdAndStatusOrderByDisplayOrderAsc(definitionId, EntityStatus.ACTIVE)
                .stream().map(this::optionMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> moderationAudits() {
        Map<String, String> definitionNames = definitionRepository.findAll().stream()
                .collect(Collectors.toMap(ProductAttributeDefinition::getId, ProductAttributeDefinition::getName));
        return auditRepository.findAllByOrderByCreatedDateDesc().stream().map(audit -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", audit.getId());
            map.put("action", audit.getAction());
            map.put("actorUserId", audit.getActorUserId());
            map.put("sourceDefinitionId", audit.getSourceDefinitionId());
            map.put("sourceDefinitionName", definitionNames.getOrDefault(audit.getSourceDefinitionId(), "Thuoc tinh da xoa"));
            map.put("targetDefinitionId", audit.getTargetDefinitionId());
            map.put("targetDefinitionName", audit.getTargetDefinitionId() == null ? null
                    : definitionNames.getOrDefault(audit.getTargetDefinitionId(), "Thuoc tinh da xoa"));
            map.put("reason", audit.getReason());
            map.put("affectedProductCount", audit.getAffectedProductCount());
            map.put("createdDate", audit.getCreatedDate());
            return map;
        }).toList();
    }

    @Transactional
    public Map<String, Object> verifyOption(String optionId) {
        ProductAttributeOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_OPTION_NOT_FOUND"));
        option.setVerified(true);
        optionRepository.save(option);
        productService.reindexAll();
        return optionMap(option);
    }

    @Transactional
    public Map<String, Object> mergeOption(String sourceId, AttributeMergeRequest request) {
        ProductAttributeOption source = optionRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_OPTION_NOT_FOUND"));
        ProductAttributeOption target = optionRepository.findById(request.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_OPTION_MERGE_TARGET_NOT_FOUND"));
        if (source.getId().equals(target.getId()) || source.getMergedIntoOptionId() != null
                || target.getMergedIntoOptionId() != null
                || !source.getDefinition().getId().equals(target.getDefinition().getId())) {
            throw new IllegalArgumentException("MERGE_OPTION_TARGET_MUST_BE_CANONICAL");
        }
        source.setMergedIntoOptionId(target.getId());
        source.setStatus(EntityStatus.INACTIVE);
        optionRepository.save(source);
        productService.reindexAll();
        return optionMap(source);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> axisInsights(String q) {
        String normalized = ProductAggregateValidator.normalize(q);
        Map<String, List<ProductVariantAxis>> groups = axisRepository.findAll().stream()
                .filter(axis -> normalized.isBlank() || axis.getNormalizedName().contains(normalized))
                .collect(Collectors.groupingBy(ProductVariantAxis::getNormalizedName, LinkedHashMap::new, Collectors.toList()));
        return groups.entrySet().stream().map(entry -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("normalizedName", entry.getKey());
            map.put("displayName", entry.getValue().get(0).getName());
            map.put("usageCount", entry.getValue().size());
            List<String> ids = entry.getValue().stream().map(ProductVariantAxis::getId).toList();
            map.put("topValues", axisValueRepository.findByAxis_IdIn(ids).stream().map(ProductVariantAxisValue::getValue).distinct().limit(20).toList());
            return map;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> axisSuggestions(String q) {
        return axisSuggestionRepository
                .findByNormalizedNameContainingIgnoreCaseAndStatusOrderByVerifiedDescNameAsc(
                        ProductAggregateValidator.normalize(q), EntityStatus.ACTIVE)
                .stream().map(this::axisSuggestionMap).toList();
    }

    @Transactional
    public Map<String, Object> createAxisSuggestion(String name) {
        String normalized = ProductAggregateValidator.normalize(name);
        VariantAxisNameSuggestion suggestion = axisSuggestionRepository.findByNormalizedName(normalized).orElseGet(() -> {
            VariantAxisNameSuggestion created = new VariantAxisNameSuggestion();
            created.setName(name.trim());
            created.setNormalizedName(normalized);
            created.setStatus(EntityStatus.ACTIVE);
            return axisSuggestionRepository.save(created);
        });
        return axisSuggestionMap(suggestion);
    }

    @Transactional
    public Map<String, Object> verifyAxisSuggestion(String id) {
        VariantAxisNameSuggestion suggestion = axisSuggestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AXIS_SUGGESTION_NOT_FOUND"));
        suggestion.setVerified(true);
        suggestion.setStatus(EntityStatus.ACTIVE);
        axisSuggestionRepository.save(suggestion);
        return axisSuggestionMap(suggestion);
    }

    @Transactional
    public Map<String, Object> mergeAxisSuggestion(String sourceId, String targetId) {
        VariantAxisNameSuggestion source = axisSuggestionRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("AXIS_SUGGESTION_NOT_FOUND"));
        VariantAxisNameSuggestion target = axisSuggestionRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("AXIS_SUGGESTION_TARGET_NOT_FOUND"));
        if (sourceId.equals(targetId) || source.getMergedIntoSuggestionId() != null || target.getMergedIntoSuggestionId() != null) {
            throw new IllegalArgumentException("AXIS_SUGGESTION_MERGE_TARGET_INVALID");
        }
        source.setMergedIntoSuggestionId(targetId);
        source.setStatus(EntityStatus.INACTIVE);
        axisSuggestionRepository.save(source);
        return axisSuggestionMap(source);
    }

    @Transactional
    public void hideAxisSuggestion(String id) {
        VariantAxisNameSuggestion suggestion = axisSuggestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AXIS_SUGGESTION_NOT_FOUND"));
        suggestion.setStatus(EntityStatus.INACTIVE);
        axisSuggestionRepository.save(suggestion);
    }

    private Map<String, Object> definitionMap(ProductAttributeDefinition definition) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", definition.getId());
        map.put("code", definition.getCode());
        map.put("name", definition.getName());
        map.put("dataType", definition.getDataType());
        map.put("defaultUnit", definition.getDefaultUnit());
        map.put("verified", definition.isVerified());
        map.put("creatorSellerId", definition.getCreatedBySellerId());
        map.put("status", definition.getStatus());
        map.put("resolvedDefinitionId", definition.getMergedIntoDefinitionId() == null ? definition.getId() : definition.getMergedIntoDefinitionId());
        map.put("productCount", valueRepository.countByDefinition_Id(definition.getId()));
        map.put("createdDate", definition.getCreatedDate());
        map.put("categoryIds", categorySuggestionRepository.findByDefinition_IdOrderByDisplayOrderAsc(definition.getId())
                .stream().map(link -> link.getCategory().getId()).toList());
        return map;
    }

    private Map<String, Object> optionMap(ProductAttributeOption option) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", option.getId());
        map.put("definitionId", option.getDefinition().getId());
        map.put("value", option.getValue());
        map.put("verified", option.isVerified());
        map.put("status", option.getStatus());
        map.put("resolvedOptionId", option.getMergedIntoOptionId() == null ? option.getId() : option.getMergedIntoOptionId());
        return map;
    }

    private Map<String, Object> axisSuggestionMap(VariantAxisNameSuggestion suggestion) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", suggestion.getId());
        map.put("name", suggestion.getName());
        map.put("verified", suggestion.isVerified());
        map.put("status", suggestion.getStatus());
        map.put("resolvedSuggestionId", suggestion.getMergedIntoSuggestionId() == null ? suggestion.getId() : suggestion.getMergedIntoSuggestionId());
        return map;
    }

    private void audit(String action, String actor, String source, String target, String reason) {
        ProductAttributeModerationAudit audit = new ProductAttributeModerationAudit();
        audit.setAction(action);
        audit.setActorUserId(actor);
        audit.setSourceDefinitionId(source);
        audit.setTargetDefinitionId(target);
        audit.setReason(reason);
        audit.setAffectedProductCount((int) valueRepository.countByDefinition_Id(source));
        auditRepository.save(audit);
    }

    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
