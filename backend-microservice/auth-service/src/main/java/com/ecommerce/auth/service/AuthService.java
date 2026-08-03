package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.request.ChangePasswordRequest;
import com.ecommerce.auth.dto.request.RegisterRequest;
import com.ecommerce.common.base.ResponseObject;

public interface AuthService {

    ResponseObject<?> register(RegisterRequest request);

    ResponseObject<?> changePassword(String email, ChangePasswordRequest request);
}
