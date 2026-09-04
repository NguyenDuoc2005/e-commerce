package com.ecommerce.order.service;

import com.ecommerce.order.entity.OrderOutboxEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class OrderOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderOutboxPublisher.class);
    private final OrderOutboxService outbox;
    private final KafkaTemplate<String, String> kafka;
    private final MeterRegistry meters;
    public OrderOutboxPublisher(OrderOutboxService outbox, KafkaTemplate<String,String> kafka){this(outbox,kafka,new io.micrometer.core.instrument.simple.SimpleMeterRegistry());}
    @Autowired public OrderOutboxPublisher(OrderOutboxService outbox, KafkaTemplate<String,String> kafka, MeterRegistry meters){this.outbox=outbox;this.kafka=kafka;this.meters=meters;}

    // Order-service has no Debezium connector yet; polling keeps the existing catalog
    // convention (transactional outbox) while avoiding a new CDC connector here.
    @Scheduled(fixedDelayString="${order.outbox.publisher-delay-ms:2000}")
    public void publishPending() {
        for (OrderOutboxEvent event : outbox.pending()) {
            String topic = "OrderSeller".equals(event.getAggregateType())
                    ? "order.event.OrderSellerCompleted" : "Dispute".equals(event.getAggregateType())
                    ? "order.event.DisputeResolved" : null;
            if (topic == null) { log.warn("Unknown outbox aggregate: {}", event.getId()); outbox.failed(event); meters.counter("order.outbox.failed","reason","unknown_event").increment(); continue; }
            try {
                kafka.send(new ProducerRecord<>(topic, event.getEventKey(), event.getPayload())).get();
                outbox.published(event);
                meters.counter("order.outbox.published","event_type",event.getEventType()).increment();
            } catch (Exception ex) {
                // Keep PENDING; the next polling cycle retries without crashing the scheduler.
                log.warn("Outbox publish failed, retaining PENDING: {}", event.getId(), ex);
                meters.counter("order.outbox.publish.failure","event_type",event.getEventType()).increment();
            }
        }
    }
}
