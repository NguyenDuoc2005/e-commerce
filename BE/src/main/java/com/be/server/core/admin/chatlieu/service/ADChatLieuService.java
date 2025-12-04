package com.be.server.core.admin.chatlieu.service;

import com.be.server.core.admin.chatlieu.model.request.ADChatLieuRequest;
import com.be.server.core.admin.chatlieu.model.request.ADChatLieuSearchRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADChatLieuService {

    ResponseObject<?> getAll(ADChatLieuSearchRequest id);

    ResponseObject<?> getById(String id);

    ResponseObject<?> modify(ADChatLieuRequest request);

    ResponseObject<?> changeStatus(String id);


}
