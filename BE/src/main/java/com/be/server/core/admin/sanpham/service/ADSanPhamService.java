package com.be.server.core.admin.sanpham.service;

import com.be.server.core.admin.sanpham.model.request.ADSanPhamRequest;
import com.be.server.core.admin.sanpham.model.request.ADSanPhamSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADSanPhamService {

    ResponseObject<?> getAll(ADSanPhamSearchRequest id);

    ResponseObject<?> getSanPhamById(String id);

    ResponseObject<?> modifySanPham(ADSanPhamRequest request);

    ResponseObject<?> changeSanPhamStatus(String id);

    ResponseObject<?> getListThuongHieu();

    ResponseObject<?> getXuatXu();

    ResponseObject<?> getListLoaiDe();

    ResponseObject<?> getListDanhMuc();

    ResponseObject<?> getListSize();

    ResponseObject<?> getListMau();

    ResponseObject<?> getListChetLieu();
}
