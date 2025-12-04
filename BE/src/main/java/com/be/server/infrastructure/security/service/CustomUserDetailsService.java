package com.be.server.infrastructure.security.service;

import com.be.server.entity.KhachHang;
import com.be.server.entity.NhanVien;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.security.repository.KhachHangAuthRepository;
import com.be.server.infrastructure.security.repository.NhanVienAuthRepository;
import com.be.server.infrastructure.security.user.UserPrincipal;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("customUserDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final KhachHangAuthRepository userAuthRepository;

    private final NhanVienAuthRepository nhanVienAuthRepository;
    private  final HttpSession httpSession;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("EMAIL: " + email);
        String role = (String) httpSession.getAttribute("role");
        if ("ADMIN".equals(role)) {
            Optional<NhanVien> existingNhanVien = nhanVienAuthRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE);
            if (existingNhanVien.isPresent()) {
                NhanVien nhanVien = existingNhanVien.get();
                System.out.println("loadUserByNhanVien: " + nhanVien);
                return UserPrincipal.createFromNhanVien(nhanVien);
            }
        }else if("USER".equals(role)) {

            Optional<KhachHang> existingUser = userAuthRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE);
            if (existingUser.isPresent()) {
                KhachHang user = existingUser.get();
                System.out.println("loadUserByKhachHang: " + user);
                return UserPrincipal.createFromKhachHang(user);
            }
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

}
