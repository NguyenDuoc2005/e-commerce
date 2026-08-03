package com.ecommerce.user.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.user.model.request.ADKhachHangSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;

public interface CustomerService {

    ResponseObject<?> getAllKhachHang(ADKhachHangSearchRequest request);

    ResponseObject<?> getKhachHangById(String id);

    ResponseObject<?> modifyKhachHang(UserUpsertRequest request);

    ResponseObject<?> updateKhachHang(UserUpsertRequest request);

    ResponseObject<?> changeKhachHangStatus(String id);

    ResponseObject<?> getLSKH(String id);
}
