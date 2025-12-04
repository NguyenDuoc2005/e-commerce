package com.be.server.core.auth.service;

import com.be.server.core.auth.dto.request.ChangePasswordRequest;
import com.be.server.core.auth.dto.request.RegisterRequest;
import com.be.server.core.common.base.ResponseObject;
import jakarta.validation.Valid;

public interface AuthService {

    ResponseObject<?> register(@Valid RegisterRequest request);

    ResponseObject<?> changePassword(String email, ChangePasswordRequest request);

}
