package com.ecommerce.auth.security;

import com.ecommerce.auth.client.UserClient;
import com.ecommerce.auth.constant.Role;
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

@Service
public class TokenProvider {

    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    @Value("${jwt.secret}")
    private String tokenSecret;

    private final UserClient userClient;

    public TokenProvider(UserClient userClient) {
        this.userClient = userClient;
    }

    public String createTokenForKhachHang(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getCustomerByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildToken(user, ACCESS_TOKEN_EXPIRATION, Role.USERS.name());
    }

    public String createRefreshTokenForKhachHang(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getCustomerByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildToken(user, REFRESH_TOKEN_EXPIRATION, Role.USERS.name());
    }

    public String createTokenForAdmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getStaffByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildToken(user, ACCESS_TOKEN_EXPIRATION, Role.ADMIN.name());
    }

    public String createRefreshTokenForAdmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getStaffByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildToken(user, REFRESH_TOKEN_EXPIRATION, Role.ADMIN.name());
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

    private String buildToken(Map<String, Object> user, long expirationMillis, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.get("email"));
        claims.put("userId", user.get("id"));
        claims.put("fullName", user.get("ten"));
        claims.put("pictureUrl", user.get("avatar"));
        claims.put("role", role);

        return buildToken(String.valueOf(user.get("email")), claims, expirationMillis);
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
