package com.ecommerce.auth.security;

import com.ecommerce.auth.entity.KhachHang;
import com.ecommerce.auth.entity.NhanVien;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final String id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String email, String password, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    public static UserPrincipal createFromKhachHang(KhachHang khachHang) {
        return new UserPrincipal(khachHang.getId(), khachHang.getEmail(), khachHang.getMatKhau(), "USERS");
    }

    public static UserPrincipal createFromNhanVien(NhanVien nhanVien) {
        return new UserPrincipal(nhanVien.getId(), nhanVien.getEmail(), nhanVien.getMatKhau(), "ADMIN");
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
