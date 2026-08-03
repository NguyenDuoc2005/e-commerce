package com.ecommerce.order.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.request.VoucherPaymentRequest;

import java.util.Map;

public interface CheckoutService {
    Object createOrder(CheckoutRequest request);

    Map<String, String> createVNPayPaymentUrl(CheckoutRequest request, String ipAddr);

    boolean handleVNPayReturn(Map<String, String> params);

    ResponseObject<?> getPhieuGiamGia(VoucherPaymentRequest request);

    ResponseObject<?> getAllApplicablePGG(String idKhachHang, Double tongTien);

    ResponseObject<?> getKhachHang(String id);
}
