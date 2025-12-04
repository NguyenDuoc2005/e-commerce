package com.be.server.core.admin.khachhang.service;

import com.be.server.core.admin.khachhang.model.request.ADKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.ADKhachHangSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADKhachHangService {

    ResponseObject<?> getAllKhachHang(ADKhachHangSearchRequest id);

    ResponseObject<?> getKhachHangById(String id);

    ResponseObject<?> modifyKhachHang(ADKhachHangRequest request);

    ResponseObject<?> updateKhachHang(ADKhachHangRequest request);

    ResponseObject<?> changeKhachHangStatus(String id);


}
