package com.ecommerce.user.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.user.model.request.ADStaffSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;

public interface EmployeeService {

    ResponseObject<?> getAllStaff(ADStaffSearchRequest request);

    ResponseObject<?> getStaffById(String id);

    ResponseObject<?> modifyStaff(UserUpsertRequest request);

    ResponseObject<?> changeStaffStatus(String id);

    ResponseObject<?> changeStaffRole(String id);

    boolean checkDuplicateField(String field, String value, String excludeId);
}
