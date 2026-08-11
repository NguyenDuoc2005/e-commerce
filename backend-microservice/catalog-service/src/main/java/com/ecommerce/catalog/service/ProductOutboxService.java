package com.ecommerce.catalog.service;

public interface ProductOutboxService {
    void publishChanged(String productId, String requestedEventType);
    void publishDeleted(String productId);
}
