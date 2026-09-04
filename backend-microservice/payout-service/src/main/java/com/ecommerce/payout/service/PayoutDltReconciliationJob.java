package com.ecommerce.payout.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;

@Component
public class PayoutDltReconciliationJob {
    private static final Logger log = LoggerFactory.getLogger(PayoutDltReconciliationJob.class);
    private static final Set<String> DLT_TOPICS = Set.of(
            "order.event.OrderSellerCompleted.DLT", "order.event.DisputeResolved.DLT");
    private final ConsumerFactory<String, String> consumerFactory;
    private final KafkaTemplate<String, String> kafka;
    private final MeterRegistry meters;
    private final String groupId;

    public PayoutDltReconciliationJob(ConsumerFactory<String, String> consumerFactory, KafkaTemplate<String, String> kafka) {
        this(consumerFactory, kafka, new io.micrometer.core.instrument.simple.SimpleMeterRegistry(),
                "payout-service-dlt-reconciliation");
    }

    @Autowired
    public PayoutDltReconciliationJob(ConsumerFactory<String, String> consumerFactory, KafkaTemplate<String, String> kafka,
                                      MeterRegistry meters,
                                      @Value("${payout.dlt.reconciliation-group:payout-service-dlt-reconciliation}") String groupId) {
        this.consumerFactory = consumerFactory; this.kafka = kafka; this.meters = meters; this.groupId = groupId;
    }

    @Scheduled(fixedDelayString = "${payout.dlt.reconciliation-delay-ms:60000}")
    public void reconcile() {
        try (Consumer<String, String> consumer =
                     consumerFactory.createConsumer(groupId, "payout-dlt-reconciliation", null, null)) {
            consumer.subscribe(DLT_TOPICS);
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofSeconds(1))) {
                String type = eventType(record.topic());
                meters.counter("payout.dlt.reconciliation.scanned", "event_type", type).increment();
                try {
                    kafka.send(new ProducerRecord<>(originalTopic(record.topic()), record.key(), record.value())).get();
                    consumer.commitSync(Map.of(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)));
                    meters.counter("payout.dlt.reconciliation.replayed", "event_type", type).increment();
                } catch (Exception ex) {
                    meters.counter("payout.dlt.reconciliation.failure", "event_type", type).increment();
                    log.error("DLT reconciliation failed: topic={}, partition={}, offset={}",
                            record.topic(), record.partition(), record.offset(), ex);
                    break;
                }
            }
        } catch (RuntimeException ex) {
            meters.counter("payout.dlt.reconciliation.poll.failure").increment();
            log.error("Cannot poll payout DLT topics", ex);
        }
    }

    static String originalTopic(String dltTopic) {
        if (!DLT_TOPICS.contains(dltTopic)) throw new IllegalArgumentException("Unsupported payout DLT topic");
        return dltTopic.substring(0, dltTopic.length() - 4);
    }

    private static String eventType(String topic) {
        return topic.substring("order.event.".length()).replace(".DLT", "");
    }
}
