package com.ecommerce.auth.security;

import com.ecommerce.auth.constant.EntityStatus;
import com.ecommerce.auth.entity.KhachHang;
import com.ecommerce.auth.entity.NhanVien;
import com.ecommerce.auth.repository.KhachHangAuthRepository;
import com.ecommerce.auth.repository.NhanVienAuthRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final KhachHangAuthRepository khachHangAuthRepository;

    private final NhanVienAuthRepository nhanVienAuthRepository;

    public CustomUserDetailsService(
            KhachHangAuthRepository khachHangAuthRepository,
            NhanVienAuthRepository nhanVienAuthRepository
    ) {
        this.khachHangAuthRepository = khachHangAuthRepository;
        this.nhanVienAuthRepository = nhanVienAuthRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String role = LoginRoleContext.get();
        if ("ADMIN".equals(role)) {
            Optional<NhanVien> existingNhanVien = nhanVienAuthRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE);
            if (existingNhanVien.isPresent()) {
                return UserPrincipal.createFromNhanVien(existingNhanVien.get());
            }
        } else if ("USER".equals(role)) {
            Optional<KhachHang> existingUser = khachHangAuthRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE);
            if (existingUser.isPresent()) {
                return UserPrincipal.createFromKhachHang(existingUser.get());
            }
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
