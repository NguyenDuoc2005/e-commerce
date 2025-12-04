package com.be.server.core.admin.loaigiay.service;

import com.be.server.core.admin.loaigiay.model.request.ADLoaiGiayRequest;
import com.be.server.core.admin.loaigiay.model.request.ADLoaiGiaySearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADLoaiGiayService {

    ResponseObject<?> getAll(ADLoaiGiaySearchRequest id);

    ResponseObject<?> getById(String id);

    ResponseObject<?> modify(ADLoaiGiayRequest request);

    ResponseObject<?> changeStatus(String id);


}
