package com.be.server.core.permitall.giohang.service;

import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.giohang.model.request.CartDetailRequest;
import com.be.server.core.permitall.giohang.model.request.CartGetAllRequest;

public interface CartService {

//    ResponseObject<?>  createCart(String idUser);

    ResponseObject<?> getAllProductCart(CartGetAllRequest req);

    ResponseObject<?> createCartDetail(CartDetailRequest req);

    ResponseObject<?>deleteCartDetail(String id);

}
