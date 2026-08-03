package com.ecommerce.order.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.model.request.ChangeStatusRequest;
import com.ecommerce.order.model.request.HoaDonDetailRequest;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.ThanhToanRequest;

public interface AdminHoaDonService {
    ResponseObject<?> getAllHoaDon(HoaDonSearchRequest request);

    ResponseObject<?> getAllHoaDonChiTiet(HoaDonDetailRequest request);

    ResponseObject<?> getLichSuTrangThai(String hoaDonId);

    ResponseObject<?> getLichSuThanhToan(String hoaDonId);

    ResponseObject<?> thanhToanHoaDon(ThanhToanRequest request);

    ResponseObject<?> changeStatus(ChangeStatusRequest request);

    byte[] generateInvoicePdf(String maHoaDon);

    byte[] generateDeliveryPdf(String maHoaDon);
}
