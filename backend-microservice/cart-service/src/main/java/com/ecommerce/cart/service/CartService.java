package com.ecommerce.cart.service;

import com.ecommerce.cart.model.request.CartDetailRequest;
import com.ecommerce.cart.model.request.CartGetAllRequest;
import com.ecommerce.common.base.ResponseObject;

public interface CartService {
    ResponseObject<?> getAllProductCart(CartGetAllRequest req);
    ResponseObject<?> createCartDetail(CartDetailRequest req);
    ResponseObject<?> deleteCartDetail(String id, String customerId);
}
