package com.be.server.core.auth.service.impl;

import com.be.server.core.auth.dto.request.ChangePasswordRequest;
import com.be.server.core.auth.dto.request.RegisterRequest;
import com.be.server.core.auth.repository.AuthUserRepository;
import com.be.server.core.auth.service.AuthService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.KhachHang;
import com.be.server.infrastructure.constant.EntityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseObject<?> register(RegisterRequest request) {
        // Validate họ tên
        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Họ tên không được để trống");
        }
        if (request.getUserName().length() < 2 || request.getUserName().length() > 50) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Họ tên phải từ 2 đến 50 ký tự");
        }

        // Validate email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Email không được để trống");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!request.getEmail().matches(emailRegex)) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Email không hợp lệ");
        }
        // Check trùng email
        Optional<KhachHang> existingUser = authUserRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        // Validate số điện thoại
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Số điện thoại không được để trống");
        }
        String phoneRegex = "^(0[3|5|7|8|9])+([0-9]{8,9})$";
        if (!request.getPhone().matches(phoneRegex)) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ");
        }
        // Check trùng số điện thoại
        Optional<KhachHang> existingPhone = authUserRepository.findBySdt(request.getPhone());
        if (existingPhone.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }

        // Validate mật khẩu
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }
        if (request.getPassword().length() < 6 || request.getPassword().length() > 100) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mật khẩu phải tối thiểu 6 ký tự và không quá 100 ký tự");
        }
        // Mật khẩu phải có cả chữ và số (chấp nhận ký tự đặc biệt)
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";
        if (!request.getPassword().matches(passwordRegex)) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mật khẩu phải chứa cả chữ và số");
        }

        // Tạo user mới khi mọi thứ hợp lệ
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        KhachHang newUser = new KhachHang();
        newUser.setTen(request.getUserName());
        newUser.setSdt(request.getPhone());
        newUser.setEmail(request.getEmail());
        newUser.setMat_khau(encodedPassword);
        newUser.setStatus(EntityStatus.ACTIVE);

        authUserRepository.save(newUser);

        return new ResponseObject<>().success("Đăng ký thành công");

    }

    @Override
    public ResponseObject<?> changePassword(String email, ChangePasswordRequest request) {
        Optional<KhachHang> optionalUser = authUserRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        KhachHang user = optionalUser.get();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getMat_khau())) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        String newPassword = request.getNewPassword();
        if (newPassword.length() < 6 || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Mật khẩu mới phải chứa cả chữ và số, tối thiểu 6 ký tự");
        }

        user.setMat_khau(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);

        return new ResponseObject<>().success("Đổi mật khẩu thành công");
    }

}
