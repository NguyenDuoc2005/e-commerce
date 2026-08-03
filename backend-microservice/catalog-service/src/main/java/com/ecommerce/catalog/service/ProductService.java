package com.ecommerce.catalog.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.model.request.ProductSearchRequest;

public interface ProductService {
    ResponseObject<?> getAll(ProductSearchRequest request);
    ResponseObject<?> getSanPhamById(String id);
    ResponseObject<?> modifySanPham(ProductRequest request);
    ResponseObject<?> changeSanPhamStatus(String id);
    ResponseObject<?> getListThuongHieu();
    ResponseObject<?> getXuatXu();
    ResponseObject<?> getListLoaiDe();
    ResponseObject<?> getListDanhMuc();
    ResponseObject<?> getListSize();
    ResponseObject<?> getListMau();
    ResponseObject<?> getListChatLieu();
    ResponseObject<?> getSanPhamMoi(ProductSearchRequest request);
    ResponseObject<?> getSanPhamGiamGia(ProductSearchRequest request);
    ResponseObject<?> getThuongHieuTrangChu(ProductSearchRequest request);
}
