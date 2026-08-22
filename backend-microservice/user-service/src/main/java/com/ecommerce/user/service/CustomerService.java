package com.ecommerce.user.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.user.model.request.ADCustomerSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;

public interface CustomerService {

    ResponseObject<?> getAllCustomer(ADCustomerSearchRequest request);

    ResponseObject<?> getCustomerById(String id);

    ResponseObject<?> modifyCustomer(UserUpsertRequest request);

    ResponseObject<?> updateCustomer(UserUpsertRequest request);

    ResponseObject<?> changeCustomerStatus(String id);

    ResponseObject<?> getLSKH(String id);
}
