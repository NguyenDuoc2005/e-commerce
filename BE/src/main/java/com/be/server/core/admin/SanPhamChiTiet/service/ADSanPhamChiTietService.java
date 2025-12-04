package com.be.server.core.admin.SanPhamChiTiet.service;

import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTRequest;
import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.banhang.model.request.giaoHangRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADSanPhamChiTietService {

    ResponseObject<?> getAll(ADSPCTSearchRequest id);

    ResponseObject<?> getDetailSPCT(String id);

    ResponseObject<?> getSPCTById(String id);

    ResponseObject<?> changeSanPhamStatus(String id);

    ResponseObject<?> getListSize();

    ResponseObject<?> getListThemSanPham();

    ResponseObject<?> getListColor();

    ResponseObject<?> modifySanPham(ADSPCTRequest request);

    ResponseObject<?> updateSanPham(ADSPCTRequest request);


}
