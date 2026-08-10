package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.client.UserClient;
import com.ecommerce.auth.dto.request.ChangePasswordRequest;
import com.ecommerce.auth.dto.request.RegisterRequest;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.common.base.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Service
@Validated
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;

    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserClient userClient, PasswordEncoder passwordEncoder) {
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseObject<?> register(RegisterRequest request) {
        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Ho ten khong duoc de trong");
        }
        if (request.getUserName().length() < 2 || request.getUserName().length() > 50) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Ho ten phai tu 2 den 50 ky tu");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Email khong duoc de trong");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!request.getEmail().matches(emailRegex)) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Email khong hop le");
        }
        if (!userClient.getCustomerByEmail(request.getEmail(), false).isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "Email da ton tai");
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "So dien thoai khong duoc de trong");
        }
        String phoneRegex = "^(0[3|5|7|8|9])+([0-9]{8,9})$";
        if (!request.getPhone().matches(phoneRegex)) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "So dien thoai khong hop le");
        }
        if (!userClient.getCustomerByPhone(request.getPhone()).isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "So dien thoai da ton tai");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mat khau khong duoc de trong");
        }
        if (request.getPassword().length() < 6 || request.getPassword().length() > 100) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mat khau phai toi thieu 6 ky tu va khong qua 100 ky tu");
        }
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";
        if (!request.getPassword().matches(passwordRegex)) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mat khau phai chua ca chu va so");
        }

        userClient.createCustomer(request.getUserName(), request.getEmail(), request.getPhone(), passwordEncoder.encode(request.getPassword()));

        return new ResponseObject<>().success("Dang ky thanh cong");
    }

    @Override
    public ResponseObject<?> changePassword(String email, ChangePasswordRequest request) {
        Map<String, Object> user = userClient.getCustomerByEmail(email, false);
        if (user.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), String.valueOf(user.get("matKhau")))) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mat khau hien tai khong dung");
        }

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < 6 || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mat khau moi phai chua ca chu va so, toi thieu 6 ky tu");
        }

        userClient.updateCustomerPassword(email, passwordEncoder.encode(newPassword));

        return new ResponseObject<>().success("Doi mat khau thanh cong");
    }
}
