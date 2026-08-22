package com.ecommerce.catalog.service;

import com.ecommerce.catalog.model.request.ProductDetailRequest;
import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.common.base.ResponseObject;

public interface ProductDetailService {
    ResponseObject<?> getAll(ProductDetailSearchRequest request);
    ResponseObject<?> getSellerAll(ProductDetailSearchRequest request, String sellerId);

    ResponseObject<?> getPublicDetail(String productId);
    ResponseObject<?> changeProductStatus(String id);
    ResponseObject<?> changeSellerProductStatus(String id, String sellerId);
    ResponseObject<?> getSPCTById(String id);
    ResponseObject<?> getSellerSPCTById(String id, String sellerId);
    ResponseObject<?> getDetailSPCT(String id);
    ResponseObject<?> getSellerDetailSPCT(String id, String sellerId);
    ResponseObject<?> getListSize();
    ResponseObject<?> getListColor();
    ResponseObject<?> getListThemProduct();
    ResponseObject<?> getSellerListProduct(String sellerId);
    ResponseObject<?> getSellerLowStock(String sellerId, Integer threshold);
    ResponseObject<?> modifyProduct(ProductDetailRequest request);
    ResponseObject<?> modifySellerProduct(ProductDetailRequest request, String sellerId);
    ResponseObject<?> updateProduct(ProductDetailRequest request);
    ResponseObject<?> updateSellerProduct(ProductDetailRequest request, String sellerId);
}
