package com.be.server.core.admin.hoadon.service;

import com.be.server.core.admin.hoadon.model.request.ADChangeStatusRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonDetailRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.admin.hoadon.model.request.ThanhToanRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADHoaDonService {

    ResponseObject<?> getAllHoaDon(ADHoaDonSearchRequest request);

    ResponseObject<?> getAllHoaDonCT(ADHoaDonDetailRequest request);

    ResponseObject<?> getAllHoaDonCT1(ADHoaDonDetailRequest request);

    ResponseObject<?> changeStatus(ADChangeStatusRequest adChangeStatusRequest);

    ResponseObject<?> getLSTTHD(String id);

    ResponseObject<?> getLSTT(String id);

    ResponseObject<?>  thanhToanHoaDon(ThanhToanRequest request);
//    ResponseObject<?> getKhachHangById(String id);
//
//    ResponseObject<?> modifyKhachHang(ADHoaDonRequest request);
//
//    ResponseObject<?> changeKhachHangStatus(String id);


}
