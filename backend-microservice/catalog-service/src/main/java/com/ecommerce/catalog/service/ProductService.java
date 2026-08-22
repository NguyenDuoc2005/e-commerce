package com.ecommerce.catalog.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.model.request.ProductSearchRequest;

public interface ProductService {
    ResponseObject<?> getAdminAll(ProductSearchRequest request);
    ResponseObject<?> getSellerAll(ProductSearchRequest request, String sellerId);
    ResponseObject<?> getAll(ProductSearchRequest request);
    ResponseObject<?> getProductById(String id);
    ResponseObject<?> getSellerProductById(String id, String sellerId);
    ResponseObject<?> modifyProduct(ProductRequest request);
    ResponseObject<?> modifySellerProduct(ProductRequest request, String sellerId);
    ResponseObject<?> changeProductStatus(String id);
    ResponseObject<?> changeSellerProductStatus(String id, String sellerId);
    ResponseObject<?> getListBrand();
    ResponseObject<?> getXuatXu();
    ResponseObject<?> getListSoleType();
    ResponseObject<?> getListCategory();
    ResponseObject<?> getListSize();
    ResponseObject<?> getListMau();
    ResponseObject<?> getListMaterial();
    ResponseObject<?> getProductMoi(ProductSearchRequest request);
    ResponseObject<?> getProductGiamGia(ProductSearchRequest request);
    ResponseObject<?> getBrandTrangChu(ProductSearchRequest request);
}
