package com.be.server.core.permitall.chitietsanpham.service;

import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.chitietsanpham.dto.request.PMSanPhamChiTietRequest;
import com.be.server.core.permitall.sanpham.dto.request.PMSanPhamMoiSearchRequest;

public interface PMSanPhamChiTietService {

    ResponseObject<?> getSanPhamChiTiet(PMSanPhamChiTietRequest request);

}
