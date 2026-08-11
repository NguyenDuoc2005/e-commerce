package com.ecommerce.catalog.service.impl;

import com.ecommerce.catalog.document.ProductDocument;
import com.ecommerce.catalog.entity.OutboxEvent;
import com.ecommerce.catalog.repository.OutboxEventRepository;
import com.ecommerce.catalog.service.ProductOutboxService;
import com.ecommerce.catalog.service.ProductSearchIndexer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductOutboxServiceImpl implements ProductOutboxService {

    public static final String PRODUCT = "Product";
    public static final String CREATED = "ProductCreated";
    public static final String UPDATED = "ProductUpdated";
    public static final String DELETED = "ProductDeleted";

    private final OutboxEventRepository outboxEventRepository;
    private final ProductSearchIndexer productSearchIndexer;
    private final ObjectMapper objectMapper;

    public ProductOutboxServiceImpl(
            OutboxEventRepository outboxEventRepository,
            ProductSearchIndexer productSearchIndexer,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.productSearchIndexer = productSearchIndexer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishChanged(String productId, String requestedEventType) {
        ProductDocument document = productSearchIndexer.buildDocument(productId).orElse(null);
        if (document == null) {
            save(productId, DELETED, null);
            return;
        }
        save(productId, requestedEventType, toJson(document));
    }

    @Override
    public void publishDeleted(String productId) {
        save(productId, DELETED, null);
    }

    private void save(String productId, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(PRODUCT);
        event.setAggregateId(productId);
        event.setEventType(eventType);
        event.setPayload(payload);
        outboxEventRepository.save(event);
    }

    private String toJson(ProductDocument document) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Khong serialize duoc product outbox payload", ex);
        }
    }
}
