package com.ecommerce.auth.security;

import com.ecommerce.auth.constant.Role;
import com.ecommerce.auth.entity.KhachHang;
import com.ecommerce.auth.entity.NhanVien;
import com.ecommerce.auth.repository.KhachHangAuthRepository;
import com.ecommerce.auth.repository.NhanVienAuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenProvider {

    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    @Value("${jwt.secret}")
    private String tokenSecret;

    private final KhachHangAuthRepository khachHangAuthRepository;

    private final NhanVienAuthRepository nhanVienAuthRepository;

    public TokenProvider(
            KhachHangAuthRepository khachHangAuthRepository,
            NhanVienAuthRepository nhanVienAuthRepository
    ) {
        this.khachHangAuthRepository = khachHangAuthRepository;
        this.nhanVienAuthRepository = nhanVienAuthRepository;
    }

    public String createTokenForKhachHang(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<KhachHang> userOpt = khachHangAuthRepository.findByEmail(userPrincipal.getEmail());
        return userOpt.map(user -> buildTokenKhachHang(user, ACCESS_TOKEN_EXPIRATION, Role.USERS.name())).orElse(null);
    }

    public String createRefreshTokenForKhachHang(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<KhachHang> userOpt = khachHangAuthRepository.findByEmail(userPrincipal.getEmail());
        return userOpt.map(user -> buildTokenKhachHang(user, REFRESH_TOKEN_EXPIRATION, Role.USERS.name())).orElse(null);
    }

    public String createTokenForAdmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<NhanVien> userOpt = nhanVienAuthRepository.findByEmail(userPrincipal.getEmail());
        return userOpt.map(user -> buildTokenAdmin(user, ACCESS_TOKEN_EXPIRATION, Role.ADMIN.name())).orElse(null);
    }

    public String createRefreshTokenForAdmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<NhanVien> userOpt = nhanVienAuthRepository.findByEmail(userPrincipal.getEmail());
        return userOpt.map(user -> buildTokenAdmin(user, REFRESH_TOKEN_EXPIRATION, Role.ADMIN.name())).orElse(null);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return getClaimsToken(token).get("email", String.class);
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(tokenSecret.getBytes());
    }

    private Claims getClaimsToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildTokenKhachHang(KhachHang user, long expirationMillis, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        claims.put("fullName", user.getTen());
        claims.put("pictureUrl", user.getAvatar());
        claims.put("role", role);

        return buildToken(user.getEmail(), claims, expirationMillis);
    }

    private String buildTokenAdmin(NhanVien nhanVien, long expirationMillis, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", nhanVien.getEmail());
        claims.put("userId", nhanVien.getId());
        claims.put("fullName", nhanVien.getTen());
        claims.put("pictureUrl", nhanVien.getAvatar());
        claims.put("role", role);

        return buildToken(nhanVien.getEmail(), claims, expirationMillis);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMillis) {
        return Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .setIssuer("glamsole")
                .signWith(getSigningKey())
                .compact();
    }
}
