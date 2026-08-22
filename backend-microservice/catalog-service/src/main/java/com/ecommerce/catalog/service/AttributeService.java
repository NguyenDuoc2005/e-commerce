package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Color;
import com.ecommerce.catalog.entity.base.CatalogAttribute;
import com.ecommerce.catalog.model.request.AttributeRequest;
import com.ecommerce.catalog.model.request.AttributeSearchRequest;
import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class AttributeService {

    private final EntityManager entityManager;

    private final Map<String, AttributeDefinition<? extends CatalogAttribute>> definitions;

    public AttributeService(EntityManager entityManager, List<AttributeDefinition<? extends CatalogAttribute>> definitions) {
        this.entityManager = entityManager;
        this.definitions = definitions.stream().collect(java.util.stream.Collectors.toMap(AttributeDefinition::key, Function.identity()));
    }

    public ResponseObject<?> getAll(String key, AttributeSearchRequest request) {
        AttributeDefinition<? extends CatalogAttribute> definition = definition(key);
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        Page<? extends CatalogAttribute> page = search(definition.entityClass(), request.getQ(), pageable);
        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, definition.listSuccessMessage());
    }

    public ResponseObject<?> getById(String key, String id) {
        AttributeDefinition<? extends CatalogAttribute> definition = definition(key);
        return findById(definition, id)
                .map(value -> new ResponseObject<>(value, HttpStatus.OK, definition.getSuccessMessage()))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, definition.notFoundMessage()));
    }

    public ResponseObject<?> modify(String key, AttributeRequest request) {
        AttributeDefinition<? extends CatalogAttribute> definition = definition(key);
        if (StringUtils.hasLength(request.getId())) {
            Optional<? extends CatalogAttribute> existing = findById(definition, request.getId());
            if (existing.isPresent()) {
                CatalogAttribute attribute = existing.get();
                attribute.setCode(request.getCode());
                attribute.setName(request.getName());
                if (attribute instanceof Color color) {
                    color.setMau(request.getColor());
                }
                save(definition, attribute);
                return new ResponseObject<>(attribute, HttpStatus.OK, definition.updateSuccessMessage());
            }
        }

        ResponseObject<?> duplicate = definition.validateDuplicate(request);
        if (duplicate != null) {
            return duplicate;
        }

        CatalogAttribute attribute = definition.newEntity();
        attribute.setCode(request.getCode());
        attribute.setName(request.getName());
        attribute.setStatus(EntityStatus.ACTIVE);
        if (attribute instanceof Color color) {
            color.setMau(request.getColor());
        }
        save(definition, attribute);
        return new ResponseObject<>(attribute, HttpStatus.CREATED, definition.createSuccessMessage());
    }

    public ResponseObject<?> changeStatus(String key, String id) {
        AttributeDefinition<? extends CatalogAttribute> definition = definition(key);
        Optional<? extends CatalogAttribute> optional = findById(definition, id);
        if (optional.isEmpty()) {
            return ResponseObject.successForward(HttpStatus.NOT_FOUND, definition.notFoundForChangeMessage());
        }
        CatalogAttribute attribute = optional.get();
        attribute.setStatus(attribute.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        save(definition, attribute);
        return ResponseObject.successForward(HttpStatus.OK, "Doi trang thai thanh cong");
    }

    private AttributeDefinition<? extends CatalogAttribute> definition(String key) {
        AttributeDefinition<? extends CatalogAttribute> definition = definitions.get(key);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown catalog attribute: " + key);
        }
        return definition;
    }

    private <T extends CatalogAttribute> Page<T> search(Class<T> entityClass, String q, Pageable pageable) {
        String queryText = """
                SELECT e FROM %s e
                WHERE (:q IS NULL OR :q = '' OR LOWER(e.code) LIKE LOWER(CONCAT('%%', :q, '%%')) OR LOWER(e.name) LIKE LOWER(CONCAT('%%', :q, '%%')))
                """.formatted(entityClass.getSimpleName());
        TypedQuery<T> query = entityManager.createQuery(queryText, entityClass);
        query.setParameter("q", q);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        String countText = """
                SELECT COUNT(e) FROM %s e
                WHERE (:q IS NULL OR :q = '' OR LOWER(e.code) LIKE LOWER(CONCAT('%%', :q, '%%')) OR LOWER(e.name) LIKE LOWER(CONCAT('%%', :q, '%%')))
                """.formatted(entityClass.getSimpleName());
        Long total = entityManager.createQuery(countText, Long.class)
                .setParameter("q", q)
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @SuppressWarnings("unchecked")
    private <T extends CatalogAttribute> Optional<T> findById(AttributeDefinition<T> definition, String id) {
        return definition.repository().findById(id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void save(AttributeDefinition definition, CatalogAttribute attribute) {
        definition.repository().save(attribute);
    }

    public interface AttributeDefinition<T extends CatalogAttribute> {
        String key();
        Class<T> entityClass();
        JpaRepository<T, String> repository();
        T newEntity();
        ResponseObject<?> validateDuplicate(AttributeRequest request);
        String listSuccessMessage();
        String getSuccessMessage();
        String notFoundMessage();
        String updateSuccessMessage();
        String createSuccessMessage();
        String notFoundForChangeMessage();
    }
}
