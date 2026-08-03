package com.ecommerce.user.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.user.model.request.ADNhanVienSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;

public interface EmployeeService {

    ResponseObject<?> getAllNhanVien(ADNhanVienSearchRequest request);

    ResponseObject<?> getNhanVienById(String id);

    ResponseObject<?> modifyNhanVien(UserUpsertRequest request);

    ResponseObject<?> changeNhanVienStatus(String id);

    ResponseObject<?> changeNhanVienRole(String id);

    boolean checkDuplicateField(String field, String value, String excludeId);
}
