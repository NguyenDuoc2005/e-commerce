package com.ecommerce.payout.service;

import com.ecommerce.payout.entity.ProcessedEvent;
import com.ecommerce.payout.model.DisputeAdjustmentRequest;
import com.ecommerce.payout.model.ReceivableRequest;
import com.ecommerce.payout.repository.ProcessedEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class OrderEventsConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);
    private final ObjectMapper mapper;
    private final PayoutService payout;
    private final ProcessedEventRepository processed;
    private final MeterRegistry meters;

    public OrderEventsConsumer(ObjectMapper mapper, PayoutService payout, ProcessedEventRepository processed) {
        this(mapper, payout, processed, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());
    }

    @Autowired public OrderEventsConsumer(ObjectMapper mapper, PayoutService payout, ProcessedEventRepository processed,
                               MeterRegistry meters) {
        this.mapper = mapper; this.payout = payout; this.processed = processed; this.meters = meters;
    }

    @KafkaListener(topics = "order.event.OrderSellerCompleted", groupId = "payout-service-order-events")
    @Transactional public void completed(String payload) { handle(payload, "OrderSellerCompleted", true); }

    @KafkaListener(topics = "order.event.DisputeResolved", groupId = "payout-service-order-events")
    @Transactional public void dispute(String payload) { handle(payload, "DisputeResolved", false); }

    private void handle(String payload, String type, boolean receivable) {
        try {
            var node = mapper.readTree(payload);
            String key = node.path(receivable ? "orderSellerId" : "disputeId").asText(null);
            if (key == null || key.isBlank()) throw new NonRetryableException("Missing event key");
            if (processed.findByEventKey(key).isPresent()) {
                meters.counter("payout.event.duplicate", "event_type", type).increment();
                log.info("duplicate event skipped: {}", key); return;
            }
            if (receivable) payout.createReceivable(mapper.treeToValue(node, ReceivableRequest.class));
            else payout.applyDisputeAdjustment(mapper.treeToValue(node, DisputeAdjustmentRequest.class));
            ProcessedEvent event = new ProcessedEvent();
            event.setEventKey(key); event.setEventType(type); processed.saveAndFlush(event);
            meters.counter("payout.event.processed", "event_type", type).increment();
        } catch (DataIntegrityViolationException ex) {
            meters.counter("payout.event.duplicate", "event_type", type).increment();
            log.info("duplicate event skipped: {}", type);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new NonRetryableException("Invalid " + type + " event", ex);
        } catch (NonRetryableException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new RetryableException("Failed to process " + type + " event", ex);
        }
    }
}
