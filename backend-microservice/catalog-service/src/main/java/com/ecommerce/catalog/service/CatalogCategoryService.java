package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.CategoryAttributeSuggestion;
import com.ecommerce.catalog.entity.ProductAttributeDefinition;
import com.ecommerce.catalog.repository.CategoryAttributeSuggestionRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductAttributeDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CatalogCategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryAttributeSuggestionRepository suggestionRepository;
    private final ProductAttributeDefinitionRepository definitionRepository;

    public CatalogCategoryService(CategoryRepository categoryRepository,
                                  CategoryAttributeSuggestionRepository suggestionRepository,
                                  ProductAttributeDefinitionRepository definitionRepository) {
        this.categoryRepository = categoryRepository;
        this.suggestionRepository = suggestionRepository;
        this.definitionRepository = definitionRepository;
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> request) {
        Category category = new Category();
        apply(category, request);
        category.setStatus(EntityStatus.ACTIVE);
        return map(categoryRepository.save(category));
    }

    @Transactional
    public Map<String, Object> update(String id, Map<String, Object> request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CATEGORY_INVALID"));
        apply(category, request);
        return map(categoryRepository.save(category));
    }

    @Transactional
    public Map<String, Object> status(String id, EntityStatus status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CATEGORY_INVALID"));
        category.setStatus(status);
        return map(categoryRepository.save(category));
    }

    @Transactional
    public void configureSuggestions(String categoryId, List<Map<String, Object>> requests) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("CATEGORY_INVALID"));
        suggestionRepository.deleteByCategory_Id(categoryId);
        int defaultOrder = 0;
        for (Map<String, Object> request : requests == null ? List.<Map<String, Object>>of() : requests) {
            String definitionId = String.valueOf(request.get("definitionId"));
            ProductAttributeDefinition definition = definitionRepository.findById(definitionId)
                    .orElseThrow(() -> new IllegalArgumentException("ATTRIBUTE_DEFINITION_NOT_FOUND"));
            CategoryAttributeSuggestion link = new CategoryAttributeSuggestion();
            link.setCategory(category);
            link.setDefinition(definition);
            link.setRequiredValue(Boolean.TRUE.equals(request.get("required")));
            link.setFilterable(Boolean.TRUE.equals(request.get("filterable")));
            Object order = request.get("displayOrder");
            link.setDisplayOrder(order instanceof Number number ? number.intValue() : defaultOrder);
            link.setStatus(EntityStatus.ACTIVE);
            suggestionRepository.save(link);
            defaultOrder++;
        }
    }

    private void apply(Category category, Map<String, Object> request) {
        String name = text(request.get("name"));
        if (name == null) throw new IllegalArgumentException("CATEGORY_NAME_REQUIRED");
        category.setName(name);
        category.setCode(text(request.get("code")) == null ? "CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : text(request.get("code")));
        category.setSlug(text(request.get("slug")) == null ? ProductAggregateValidator.normalize(name).replace(' ', '-') : text(request.get("slug")));
        Object order = request.get("displayOrder");
        category.setDisplayOrder(order instanceof Number number ? number.intValue() : 0);
        String parentId = text(request.get("parentId"));
        if (parentId != null) {
            if (parentId.equals(category.getId())) throw new IllegalArgumentException("CATEGORY_PARENT_INVALID");
            category.setParent(categoryRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("CATEGORY_PARENT_INVALID")));
        } else {
            category.setParent(null);
        }
    }

    private Map<String, Object> map(Category category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", category.getId());
        map.put("code", category.getCode());
        map.put("name", category.getName());
        map.put("slug", category.getSlug());
        map.put("parentId", category.getParent() == null ? null : category.getParent().getId());
        map.put("displayOrder", category.getDisplayOrder());
        map.put("status", category.getStatus());
        return map;
    }

    private static String text(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return String.valueOf(value).trim();
    }
}
