package com.be.server.core.admin.kichthuoc.service;

import com.be.server.core.admin.kichthuoc.model.request.ADKichThuocRequest;
import com.be.server.core.admin.kichthuoc.model.request.ADKichThuocSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADKichThuocService {

    ResponseObject<?> getAllSize(ADKichThuocSearchRequest id);

    ResponseObject<?> getSizeById(String id);

    ResponseObject<?> modifySize(ADKichThuocRequest request);

    ResponseObject<?> changeSizeStatus(String id);


}
