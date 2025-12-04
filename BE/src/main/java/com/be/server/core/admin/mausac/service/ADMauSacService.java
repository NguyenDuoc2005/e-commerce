package com.be.server.core.admin.mausac.service;

import com.be.server.core.admin.mausac.model.request.ADMauSacRequest;
import com.be.server.core.admin.mausac.model.request.ADMauSacSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADMauSacService {

    ResponseObject<?> getAllMauSac(ADMauSacSearchRequest id);

    ResponseObject<?> getMauSacById(String id);

    ResponseObject<?> modifyMauSac(ADMauSacRequest request);

    ResponseObject<?> changeMauSacStatus(String id);


}
