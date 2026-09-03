package com.ecommerce.order.service;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderCheckoutDataService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderCheckoutDataService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public String voucherId(String orderId) {
        return orderRepository.findById(orderId).map(Order::getVoucherId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> items(String orderId) {
        return orderItemRepository.findByHoaDonId(orderId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkoutResponse(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", order.getId());
        response.put("code", order.getCode());
        response.put("customerId", order.getCustomerId());
        response.put("status", order.getTrangThaiOrder());
        response.put("orderStatus", order.getTrangThaiOrder());
        response.put("totalAmount", order.getTongTien());
        response.put("totalAfterDiscount", order.getTongTienSauGiam());
        response.put("discountAmount", order.getGiamGia());
        response.put("shippingFee", order.getPhiVanCdistrict());
        return response;
    }
}
