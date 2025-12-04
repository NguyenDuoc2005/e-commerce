package com.be.server.infrastructure.security.user;

import com.be.server.entity.KhachHang;
import com.be.server.entity.NhanVien;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Slf4j
public class UserPrincipal implements OAuth2User, UserDetails {

    @Getter private final String id;
    @Getter private final String email;
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    @Setter
    private Map<String, Object> attributes;

    // Constructor
    public UserPrincipal(String id, String email, String password, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    // Tạo từ KhachHang
    public static UserPrincipal createFromKhachHang(KhachHang khachHang) {
        return new UserPrincipal(
                khachHang.getId(),
                khachHang.getEmail(),
                khachHang.getMat_khau(),
                "USERS"
        );
    }

    // Tạo từ NhanVien
    public static UserPrincipal createFromNhanVien(NhanVien nhanVien) {
        return new UserPrincipal(
                nhanVien.getId(),
                nhanVien.getEmail(),
                nhanVien.getMatKhau(),
                "ADMIN"
        );
    }

    // Tạo OAuth2
    public static UserPrincipal createFromKhachHangOAuth(KhachHang khachHang, Map<String, Object> attributes) {
        UserPrincipal principal = createFromKhachHang(khachHang);
        principal.setAttributes(attributes);
        return principal;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public String getPassword() {
        return password;
    }

    @Override public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override public boolean isEnabled() {
        return true;
    }

    @Override public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override public String getName() {
        return id;
    }
}
