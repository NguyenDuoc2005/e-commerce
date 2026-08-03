package com.ecommerce.catalog.service;

import com.ecommerce.catalog.model.request.ProductDetailRequest;
import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.common.base.ResponseObject;

public interface ProductDetailService {
    ResponseObject<?> getAll(ProductDetailSearchRequest request);
    ResponseObject<?> changeSanPhamStatus(String id);
    ResponseObject<?> getSPCTById(String id);
    ResponseObject<?> getDetailSPCT(String id);
    ResponseObject<?> getListSize();
    ResponseObject<?> getListColor();
    ResponseObject<?> getListThemSanPham();
    ResponseObject<?> modifySanPham(ProductDetailRequest request);
    ResponseObject<?> updateSanPham(ProductDetailRequest request);
}
