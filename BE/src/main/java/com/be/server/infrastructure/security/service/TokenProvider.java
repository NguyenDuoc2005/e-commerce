package com.be.server.infrastructure.security.service;

import com.be.server.entity.KhachHang;
import com.be.server.entity.NhanVien;
import com.be.server.infrastructure.constant.Role;
import com.be.server.infrastructure.security.repository.KhachHangAuthRepository;
import com.be.server.infrastructure.security.repository.NhanVienAuthRepository;
import com.be.server.infrastructure.security.user.UserPrincipal;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

@Slf4j
@Service
public class TokenProvider {

    @Value("${jwt.secret}")
    private String tokenSecret;

    private final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;  // 2h
    private final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 ngày

    @Setter(onMethod_ = @Autowired)
    private KhachHangAuthRepository khachHangAuthRepository;

    @Setter(onMethod_ = @Autowired)
    private NhanVienAuthRepository nhanVienAuthRepository;

    @Setter(onMethod_ = @Autowired)
    private HttpServletRequest httpServletRequest;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(tokenSecret.getBytes());
    }

    // ===== TOKEN CHO KHÁCH HÀNG =====
    public String createTokenForKhachHang(Authentication authentication) {
        log.info("Tạo access token cho khách hàng...");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<KhachHang> userOpt = khachHangAuthRepository.findByEmail(userPrincipal.getEmail());

        if (userOpt.isEmpty()) {
            log.warn("Không tìm thấy khách hàng: {}", userPrincipal.getEmail());
            return null;
        }

        return buildTokenKhachHang(userOpt.get(), ACCESS_TOKEN_EXPIRATION, Role.USERS.name());
    }

    public String createRefreshTokenForKhachHang(Authentication authentication) {
        log.info("Tạo refresh token cho khách hàng...");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<KhachHang> userOpt = khachHangAuthRepository.findByEmail(userPrincipal.getEmail());

        if (userOpt.isEmpty()) {
            log.warn("Không tìm thấy khách hàng: {}", userPrincipal.getEmail());
            return null;
        }

        return buildTokenKhachHang(userOpt.get(), REFRESH_TOKEN_EXPIRATION,  Role.USERS.name());
    }

    // ===== TOKEN CHO ADMIN =====
    public String createTokenForAdmin(Authentication authentication) {
        log.info("Tạo access token cho nhân viên...");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<NhanVien> userOpt = nhanVienAuthRepository.findByEmail(userPrincipal.getEmail());

        if (userOpt.isEmpty()) {
            log.warn("Không tìm thấy khách hàng: {}", userPrincipal.getEmail());
            return null;
        }

        return buildTokenAdmin(userOpt.get(), ACCESS_TOKEN_EXPIRATION, Role.ADMIN.name());

    }

    public String createRefreshTokenForAdmin(Authentication authentication) {
        log.info("Tạo refresh token cho nhân viên...");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<NhanVien> userOpt = nhanVienAuthRepository.findByEmail(userPrincipal.getEmail());

        if (userOpt.isEmpty()) {
            log.warn("Không tìm thấy khách hàng: {}", userPrincipal.getEmail());
            return null;
        }

        return buildTokenAdmin(userOpt.get(), REFRESH_TOKEN_EXPIRATION,  Role.ADMIN.name());
    }

    // ===== BUILD TOKEN =====
    private String buildTokenKhachHang(KhachHang user, long expirationMillis, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        claims.put("fullName", user.getTen());
        claims.put("pictureUrl", user.getAvatar());
        claims.put("role", role);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .setIssuer("glamsole")
                .signWith(getSigningKey())
                .compact();
    }

    private String buildTokenAdmin(NhanVien nhanVien, long expirationMillis, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", nhanVien.getEmail());
        claims.put("userId", nhanVien.getId());
        claims.put("fullName", nhanVien.getTen());
        claims.put("pictureUrl", nhanVien.getAvatar());
        claims.put("role", role);

        return Jwts.builder()
                .setSubject(nhanVien.getEmail())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .setIssuer("glamsole")
                .signWith(getSigningKey())
                .compact();
    }

    // ===== VALIDATION + PARSE =====
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Token không hợp lệ: {}", ex.getMessage());
            return false;
        }
    }

    private Claims getClaimsToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserIdFromToken(String token) {
        return getClaimsToken(token).get("userId", String.class);
    }

    public String getEmailFromToken(String token) {
        return getClaimsToken(token).get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        return getClaimsToken(token).get("role", String.class);
    }
}
