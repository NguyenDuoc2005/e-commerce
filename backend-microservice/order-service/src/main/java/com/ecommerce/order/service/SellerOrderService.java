package com.ecommerce.order.service;

import java.util.List;
import java.util.Map;

public interface SellerOrderService {
    List<Map<String, Object>> list(String sellerId, Integer status, String q);
    Map<String, Object> detail(String sellerId, String orderSellerId);
    Map<String, Object> changeStatus(String sellerId, String orderSellerId, String action);
    Map<String, Object> dashboard(String sellerId);
}
