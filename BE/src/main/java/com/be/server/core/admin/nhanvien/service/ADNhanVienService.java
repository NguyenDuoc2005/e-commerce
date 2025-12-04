package com.be.server.core.admin.nhanvien.service;

import com.be.server.core.admin.nhanvien.model.request.ADNhanVienRequest;
import com.be.server.core.admin.nhanvien.model.request.ADNhanVienSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADNhanVienService {

    ResponseObject<?> getAllNhanVien(ADNhanVienSearchRequest id);

    ResponseObject<?> getNhanVienById(String id);

    ResponseObject<?> modifyMNhanVien(ADNhanVienRequest request);

    ResponseObject<?> changeNhanVienStatus(String id);

    ResponseObject<?> changeNhanVienRole(String id);

    boolean checkDuplicateField(String field, String value, String excludeId);

}
