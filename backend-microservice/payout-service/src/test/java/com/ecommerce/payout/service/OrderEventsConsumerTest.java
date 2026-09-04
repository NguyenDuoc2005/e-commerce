package com.ecommerce.payout.service;

import com.ecommerce.payout.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventsConsumerTest {
    @Mock PayoutService payout;
    @Mock ProcessedEventRepository processed;

    @Test
    void malformedPayloadIsNonRetryable() {
        OrderEventsConsumer consumer = new OrderEventsConsumer(new ObjectMapper(), payout, processed,
                new SimpleMeterRegistry());
        assertThrows(NonRetryableException.class, () -> consumer.completed("not-json"));
        verifyNoInteractions(payout);
    }

    @Test
    void downstreamFailureIsRetryableAndIsNotMarkedProcessed() {
        when(processed.findByEventKey("os-1")).thenReturn(Optional.empty());
        when(payout.createReceivable(any())).thenThrow(new RuntimeException("database unavailable"));
        OrderEventsConsumer consumer = new OrderEventsConsumer(new ObjectMapper(), payout, processed,
                new SimpleMeterRegistry());
        assertThrows(RetryableException.class, () -> consumer.completed("{\"orderSellerId\":\"os-1\"}"));
        verify(processed, never()).saveAndFlush(any());
    }

    @Test
    void duplicateIsSkippedBeforeBusinessSideEffect() {
        com.ecommerce.payout.entity.ProcessedEvent event = new com.ecommerce.payout.entity.ProcessedEvent();
        when(processed.findByEventKey("os-1")).thenReturn(Optional.of(event));
        OrderEventsConsumer consumer = new OrderEventsConsumer(new ObjectMapper(), payout, processed,
                new SimpleMeterRegistry());
        consumer.completed("{\"orderSellerId\":\"os-1\"}");
        verifyNoInteractions(payout);
        verify(processed, never()).saveAndFlush(any());
    }
}
