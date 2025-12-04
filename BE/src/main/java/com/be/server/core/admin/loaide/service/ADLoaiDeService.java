package com.be.server.core.admin.loaide.service;

import com.be.server.core.admin.loaide.model.request.ADLoaiDeRequest;
import com.be.server.core.admin.loaide.model.request.ADLoaiDeSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADLoaiDeService {

    ResponseObject<?> getAll(ADLoaiDeSearchRequest id);

    ResponseObject<?> getById(String id);

    ResponseObject<?> modify(ADLoaiDeRequest request);

    ResponseObject<?> changeStatus(String id);


}
