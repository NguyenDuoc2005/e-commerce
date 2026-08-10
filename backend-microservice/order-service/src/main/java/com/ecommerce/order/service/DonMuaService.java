package com.ecommerce.order.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.model.request.HoaDonDetailRequest;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.SanPhamChiTietSearchRequest;
import com.ecommerce.order.model.request.ThemSanPhamRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;

import java.util.List;
import java.util.Map;

public interface DonMuaService {
    ResponseObject<?> getAllHoaDon(HoaDonSearchRequest request);

    ResponseObject<?> getAllHoaDonByCode(String code);

    ResponseObject<?> getHoaDonChiTiet(HoaDonDetailRequest request);

    ResponseObject<?> getAllSanPhamChiTiet(SanPhamChiTietSearchRequest request);

    ResponseObject<?> suaThongTin(UpdateDeliveryRequest request);

    ResponseObject<?> themSanPham(ThemSanPhamRequest request);

    List<Map<String, Object>> getCustomerOrderHistory(String customerId);
}
