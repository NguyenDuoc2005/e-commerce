package com.ecommerce.payout.service;

import com.ecommerce.payout.model.EventReplayRequest;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventReplayServiceTest {
    @Test
    void replayDltPublishesToOriginalWhitelistedTopic() throws Exception {
        KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
        when(kafka.send(org.mockito.ArgumentMatchers.<org.apache.kafka.clients.producer.ProducerRecord<String, String>>any()))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
        EventReplayService service = new EventReplayService(kafka);
        EventReplayRequest request = new EventReplayRequest();
        request.setTopic("order.event.DisputeResolved.DLT"); request.setKey("d-1"); request.setPayload("{}");
        assertEquals("order.event.DisputeResolved", service.replay(request));
        verify(kafka).send(argThat((org.apache.kafka.clients.producer.ProducerRecord<String, String> record) ->
                record.topic().equals("order.event.DisputeResolved")
                && record.key().equals("d-1")));
    }

    @Test
    void replayRejectsUnknownTopic() {
        EventReplayService service = new EventReplayService(mock(KafkaTemplate.class));
        EventReplayRequest request = new EventReplayRequest();
        request.setTopic("some.other.topic"); request.setPayload("{}");
        assertThrows(IllegalArgumentException.class, () -> service.replay(request));
    }
}
