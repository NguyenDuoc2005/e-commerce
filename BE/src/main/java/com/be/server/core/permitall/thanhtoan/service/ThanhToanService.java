package com.be.server.core.permitall.thanhtoan.service;

import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.thanhtoan.model.request.PMPhieuGiamGia;
import com.be.server.core.permitall.thanhtoan.model.request.PMThanhToan;
import com.be.server.entity.HoaDon;

import java.util.Map;

public interface ThanhToanService {

     HoaDon createOrder(PMThanhToan order);

     Map<String, String> createVNPayPaymentUrl(HoaDon order, String ipAddr);

     String handleVNPayReturn(Map<String, String> vnpParams);

    ResponseObject<?> getAllApplicablePGG(PMPhieuGiamGia req);

    ResponseObject<?> getPhieuGiamGia(PMPhieuGiamGia req);
}
