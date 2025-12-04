package com.be.server.core.admin.xuatxu.service;

import com.be.server.core.admin.xuatxu.model.request.ADXuatXuRequest;
import com.be.server.core.admin.xuatxu.model.request.ADXuatXuSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADXuatXuService {

    ResponseObject<?> getAllXuatXu(ADXuatXuSearchRequest id);

    ResponseObject<?> getXuatXuById(String id);

    ResponseObject<?> modifyXuatXu(ADXuatXuRequest request);

    ResponseObject<?> changeXuatXuStatus(String id);


}
