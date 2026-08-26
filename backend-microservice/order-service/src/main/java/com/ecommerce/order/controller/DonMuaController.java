package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.ChangeStatusRequest;
import com.ecommerce.order.model.request.OrderDetailRequest;
import com.ecommerce.order.model.request.OrderSearchRequest;
import com.ecommerce.order.model.request.ProductVariantSearchRequest;
import com.ecommerce.order.model.request.ThemProductRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;
import com.ecommerce.order.service.DonMuaService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/buyer/orders")
public class DonMuaController {

    private final DonMuaService donMuaService;

    public DonMuaController(DonMuaService donMuaService) {
        this.donMuaService = donMuaService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @ModelAttribute OrderSearchRequest request,
            @RequestHeader("X-User-Id") String customerId
    ) {
        request.setQ(customerId);
        return ResponseEntity.ok(donMuaService.getAllOrder(request));
    }

    @GetMapping("/grouped")
    public ResponseEntity<?> grouped(@RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok(Map.of("data", donMuaService.getGroupedCustomerOrderHistory(customerId)));
    }

    @GetMapping("/spct")
    public ResponseEntity<?> getAllProductVariant(@ModelAttribute ProductVariantSearchRequest request) {
        return ResponseEntity.ok(donMuaService.getAllProductVariant(request));
    }

    @GetMapping("/all/{code}")
    public ResponseEntity<?> getAllByCode(
            @PathVariable String code,
            @RequestHeader("X-User-Id") String customerId
    ) {
        requireOwnership(customerId, code);
        return ResponseEntity.ok(donMuaService.getAllOrderByCode(code));
    }

    @PostMapping("/sua-thong-tin")
    public ResponseEntity<?> suaThongTin(
            @ModelAttribute UpdateDeliveryRequest request,
            @RequestHeader("X-User-Id") String customerId
    ) {
        requireOwnership(customerId, request.getMaOrder());
        return ResponseEntity.ok(donMuaService.suaThongTin(request));
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatus(
            @ModelAttribute ChangeStatusRequest request,
            @RequestHeader("X-User-Id") String customerId
    ) {
        requireOwnership(customerId, request.getMaOrder());
        if (request.getStatus() == null || request.getStatus() != com.ecommerce.order.constant.OrderStatusConstant.DA_HUY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Buyer chi duoc huy don hang"));
        }
        return ResponseEntity.ok(donMuaService.cancelOrder(request));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getOrderItem(
            @ModelAttribute OrderDetailRequest request,
            @RequestHeader("X-User-Id") String customerId
    ) {
        requireOwnership(customerId, request.getMaOrder());
        return ResponseEntity.ok(donMuaService.getOrderItem(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLichSuOrder(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String customerId
    ) {
        requireOwnership(customerId, id);
        return ResponseEntity.ok(donMuaService.getOrderStatusHistory(id));
    }

    @PostMapping("them-san-pham")
    public ResponseEntity<?> themProduct(@ModelAttribute ThemProductRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Khong ho tro them san pham vao don da tao"));
    }

    @GetMapping("/payment_history/{id}")
    public ResponseEntity<?> getPaymentHistory(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String customerId
    ) {
        requireOwnership(customerId, id);
        return ResponseEntity.ok(donMuaService.getPaymentHistory(id));
    }

    private void requireOwnership(String customerId, String orderReference) {
        if (!donMuaService.customerOwnsOrder(customerId, orderReference)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay don hang");
        }
    }
}
