package com.ecommerce.order.service;

import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PromotionClient;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderSagaStep;
import com.ecommerce.order.entity.OrderSagaStepName;
import com.ecommerce.order.model.request.CheckoutProductItem;
import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.service.OrderCheckoutSagaExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCheckoutSagaExecutorTest {
    @Mock private CatalogClient catalogClient;
    @Mock private PromotionClient promotionClient;
    @Mock private CartClient cartClient;
    @Mock private OrderSagaStepPersistence persistence;
    @Mock private OrderCheckoutDataService orderData;

    @Test
    void voucherFailurePersistsFailureAndCompensatesStock() {
        OrderSagaStep stock = step("stock-step", OrderSagaStepName.STOCK_DEDUCTED);
        OrderSagaStep voucher = step("voucher-step", OrderSagaStepName.VOUCHER_APPLIED);
        when(persistence.pending("order-1", OrderSagaStepName.STOCK_DEDUCTED)).thenReturn(stock);
        when(persistence.pending("order-1", OrderSagaStepName.VOUCHER_APPLIED)).thenReturn(voucher);
        when(persistence.successful("order-1")).thenReturn(List.of(stock));
        when(orderData.voucherId("order-1")).thenReturn("voucher-1");
        when(orderData.items("order-1")).thenReturn(List.of(item("variant-1", 2)));
        doNothing().when(catalogClient).adjustStock("variant-1", -2);
        doThrow(new IllegalStateException("promotion unavailable"))
                .when(promotionClient).decrementVoucher("voucher-1");

        assertThrows(IllegalStateException.class, () -> executor().execute("order-1", request("voucher-code", 2)));

        verify(persistence).success(stock.getId());
        verify(persistence).failed(eq(voucher.getId()), any(String.class));
        verify(persistence).compensating(stock.getId());
        verify(catalogClient).adjustStock("variant-1", 2);
        verify(persistence).compensated(stock.getId());
        verify(cartClient, never()).deleteItems(any(), any());
    }

    @Test
    void compensationFailureIsDurableAndDoesNotThrowCompensationException() {
        OrderSagaStep stock = step("stock-step", OrderSagaStepName.STOCK_DEDUCTED);
        OrderSagaStep voucher = step("voucher-step", OrderSagaStepName.VOUCHER_APPLIED);
        when(persistence.pending("order-1", OrderSagaStepName.STOCK_DEDUCTED)).thenReturn(stock);
        when(persistence.pending("order-1", OrderSagaStepName.VOUCHER_APPLIED)).thenReturn(voucher);
        when(persistence.successful("order-1")).thenReturn(List.of(stock));
        when(orderData.voucherId("order-1")).thenReturn("voucher-1");
        when(orderData.items("order-1")).thenReturn(List.of(item("variant-1", 1)));
        doNothing().when(catalogClient).adjustStock("variant-1", -1);
        doThrow(new IllegalStateException("promotion unavailable"))
                .when(promotionClient).decrementVoucher("voucher-1");
        doThrow(new IllegalStateException("catalog still unavailable"))
                .when(catalogClient).adjustStock("variant-1", 1);

        assertThrows(IllegalStateException.class, () -> executor().execute("order-1", request("voucher-code", 1)));

        verify(persistence).compensationFailed(eq(stock.getId()), any(String.class));
        verify(persistence, never()).compensated(stock.getId());
    }

    private OrderCheckoutSagaExecutor executor() {
        return new OrderCheckoutSagaExecutor(catalogClient, promotionClient, cartClient, persistence, orderData);
    }

    private static OrderItem item(String variantId, int quantity) {
        OrderItem item = new OrderItem();
        item.setProductVariantId(variantId);
        item.setQuantity(quantity);
        return item;
    }

    private static OrderSagaStep step(String id, OrderSagaStepName name) {
        OrderSagaStep step = new OrderSagaStep();
        step.setId(id);
        step.setOrderId("order-1");
        step.setStepName(name);
        return step;
    }

    private static CheckoutRequest request(String voucher, int quantity) {
        CheckoutProductItem item = new CheckoutProductItem();
        item.setId("variant-1");
        item.setQuantity(quantity);
        CheckoutRequest request = new CheckoutRequest();
        request.setCustomer("customer-1");
        request.setMaGiamGia(voucher);
        request.setProduct(List.of(item));
        return request;
    }
}
