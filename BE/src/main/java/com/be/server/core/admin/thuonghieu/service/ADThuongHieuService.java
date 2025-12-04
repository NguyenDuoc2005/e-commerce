package com.be.server.core.admin.thuonghieu.service;

import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuRequest;
import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADThuongHieuService {

    ResponseObject<?> getAllThuongHieu(ADThuongHieuSearchRequest id);

    ResponseObject<?> getThuongHieuById(String id);

    ResponseObject<?> modifyThuongHieu(ADThuongHieuRequest request);

    ResponseObject<?> changeSizeStatus(String id);


}
