package com.be.server.core.permitall.donmua.service;

import com.be.server.core.admin.banhang.model.request.ADThemSanPhamRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.donmua.model.request.PMThemSanPhamRequest;
import com.be.server.core.permitall.donmua.model.request.UpdateDeliveryDTO;

public interface DonMuaService {

    ResponseObject<?> getAllHoaDon(ADHoaDonSearchRequest request);

    ResponseObject<?> getAllHoaDon1(String code);

    ResponseObject<?> createThemSanPham(PMThemSanPhamRequest adThemSanPhamRequest);

    ResponseObject<?>  suaThongTin(UpdateDeliveryDTO request);

    ResponseObject<?> getLSTT(String id);
}
