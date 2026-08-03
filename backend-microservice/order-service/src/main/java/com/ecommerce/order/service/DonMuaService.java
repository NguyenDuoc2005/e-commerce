package com.ecommerce.order.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.SanPhamChiTietSearchRequest;
import com.ecommerce.order.model.request.ThemSanPhamRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;

public interface DonMuaService {
    ResponseObject<?> getAllHoaDon(HoaDonSearchRequest request);

    ResponseObject<?> getAllHoaDonByCode(String code);

    ResponseObject<?> getAllSanPhamChiTiet(SanPhamChiTietSearchRequest request);

    ResponseObject<?> suaThongTin(UpdateDeliveryRequest request);

    ResponseObject<?> themSanPham(ThemSanPhamRequest request);
}
