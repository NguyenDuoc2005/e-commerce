package com.ecommerce.order.service.impl;

import com.ecommerce.order.client.NotificationClient;
import com.ecommerce.order.client.PayoutClient;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.constant.OrderStatusConstant;
import com.ecommerce.order.repository.OrderSellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerOrderSoldCountTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private OrderSellerRepository orderSellerRepository;
    @Mock private PayoutClient payoutClient;
    @Mock private NotificationClient notificationClient;
    @Mock private CatalogClient catalogClient;
    @Mock private OrderSellerRepository.SellerSoldCount soldCount;

    @Test
    void sumsOnlyCompletedOrderItemsAndKeepsZeroForSellerWithoutSales() {
        when(soldCount.getSellerId()).thenReturn("seller-1");
        when(soldCount.getSoldCount()).thenReturn(7L);
        when(orderSellerRepository.findSoldCounts(
                List.of("seller-1", "seller-2"), OrderStatusConstant.HOAN_THANH.ordinal()))
                .thenReturn(List.of(soldCount));
        SellerOrderServiceImpl service = new SellerOrderServiceImpl(
                jdbcTemplate, orderSellerRepository, payoutClient, notificationClient, catalogClient);

        Map<String, Long> result = service.soldCounts(List.of("seller-1", "seller-2", "seller-1"));

        assertEquals(7L, result.get("seller-1"));
        assertEquals(0L, result.get("seller-2"));
    }
}
