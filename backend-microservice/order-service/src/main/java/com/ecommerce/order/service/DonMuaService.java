package com.ecommerce.order.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.model.request.ChangeStatusRequest;
import com.ecommerce.order.model.request.OrderDetailRequest;
import com.ecommerce.order.model.request.OrderSearchRequest;
import com.ecommerce.order.model.request.ProductVariantSearchRequest;
import com.ecommerce.order.model.request.ThemProductRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;

import java.util.List;
import java.util.Map;

public interface DonMuaService {
    ResponseObject<?> getAllOrder(OrderSearchRequest request);

    ResponseObject<?> getAllOrderByCode(String code);

    ResponseObject<?> getOrderItem(OrderDetailRequest request);

    ResponseObject<?> getAllProductVariant(ProductVariantSearchRequest request);

    ResponseObject<?> suaThongTin(UpdateDeliveryRequest request);

    ResponseObject<?> cancelOrder(ChangeStatusRequest request);

    ResponseObject<?> getOrderStatusHistory(String orderId);

    ResponseObject<?> getPaymentHistory(String orderId);

    ResponseObject<?> themProduct(ThemProductRequest request);

    List<Map<String, Object>> getCustomerOrderHistory(String customerId);

    List<Map<String, Object>> getGroupedCustomerOrderHistory(String customerId);

    Map<String, Object> reviewEligibility(String customerId, String orderSellerId, String productDetailId);

    boolean customerOwnsOrder(String customerId, String orderReference);
}
