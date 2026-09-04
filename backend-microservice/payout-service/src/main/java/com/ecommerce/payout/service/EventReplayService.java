package com.ecommerce.payout.service;

import com.ecommerce.payout.model.EventReplayRequest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventReplayService {
    private final KafkaTemplate<String, String> kafka;
    public EventReplayService(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }
    public String replay(EventReplayRequest request) {
        if (request == null || request.getPayload() == null || request.getPayload().isBlank())
            throw new IllegalArgumentException("Replay payload is required");
        String topic = request.getTopic();
        if (topic == null || topic.isBlank()) throw new IllegalArgumentException("Replay topic is required");
        if (topic.endsWith(".DLT")) topic = topic.substring(0, topic.length() - 4);
        if (!topic.equals("order.event.OrderSellerCompleted") && !topic.equals("order.event.DisputeResolved"))
            throw new IllegalArgumentException("Unsupported replay topic");
        try { kafka.send(new ProducerRecord<>(topic, request.getKey(), request.getPayload())).get(); return topic; }
        catch (Exception ex) { throw new RetryableException("Replay publish failed", ex); }
    }
}
