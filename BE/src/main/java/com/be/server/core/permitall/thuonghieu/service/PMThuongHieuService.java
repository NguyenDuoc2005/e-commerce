package com.be.server.core.permitall.thuonghieu.service;

import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.thuonghieu.dto.request.PMThuongHieuSearchRequest;

public interface PMThuongHieuService {

    ResponseObject<?> getAllThuongHieu(PMThuongHieuSearchRequest request);

}
