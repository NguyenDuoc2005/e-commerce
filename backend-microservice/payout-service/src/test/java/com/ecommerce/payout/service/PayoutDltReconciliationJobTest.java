package com.ecommerce.payout.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PayoutDltReconciliationJobTest {
    @Test void mapsOrderSellerDltToOriginalTopic() {
        assertEquals("order.event.OrderSellerCompleted",
                PayoutDltReconciliationJob.originalTopic("order.event.OrderSellerCompleted.DLT"));
    }
    @Test void mapsDisputeDltToOriginalTopic() {
        assertEquals("order.event.DisputeResolved",
                PayoutDltReconciliationJob.originalTopic("order.event.DisputeResolved.DLT"));
    }
    @Test void rejectsNonPayoutDltTopic() {
        assertThrows(IllegalArgumentException.class,
                () -> PayoutDltReconciliationJob.originalTopic("checkout.event.DLT"));
    }
}
