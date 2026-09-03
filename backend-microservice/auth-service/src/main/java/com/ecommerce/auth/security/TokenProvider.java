package com.ecommerce.auth.security;

import com.ecommerce.auth.client.UserClient;
import com.ecommerce.auth.client.SellerClient;
import com.ecommerce.auth.constant.Role;
import com.ecommerce.common.base.AuthTokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenProvider {

    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    @Value("${jwt.secret}")
    private String tokenSecret;

    private final UserClient userClient;
    private final SellerClient sellerClient;

    public TokenProvider(UserClient userClient, SellerClient sellerClient) {
        this.userClient = userClient;
        this.sellerClient = sellerClient;
    }

    public String createTokenForCustomer(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getCustomerByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildCustomerToken(user, ACCESS_TOKEN_EXPIRATION, ACCESS_TOKEN_TYPE);
    }

    public String createRefreshTokenForCustomer(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getCustomerByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildCustomerToken(user, REFRESH_TOKEN_EXPIRATION, REFRESH_TOKEN_TYPE);
    }

    public String createTokenForAdmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getStaffByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildToken(user, ACCESS_TOKEN_EXPIRATION, Role.ADMIN.name(), ACCESS_TOKEN_TYPE);
    }

    public String createRefreshTokenForAdmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> user = userClient.getStaffByEmail(userPrincipal.getEmail(), false);
        return user.isEmpty() ? null : buildToken(user, REFRESH_TOKEN_EXPIRATION, Role.ADMIN.name(), REFRESH_TOKEN_TYPE);
    }

    public AuthTokens refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank() || !validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token khong hop le hoac da het han");
        }

        Claims claims = getClaimsToken(refreshToken);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get("tokenType", String.class))) {
            throw new IllegalArgumentException("Token cung cap khong phai refresh token");
        }

        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        if (Role.ADMIN.name().equals(role)) {
            Map<String, Object> user = userClient.getStaffByEmail(email, true);
            if (user == null || user.isEmpty()) {
                throw new IllegalArgumentException("Tai khoan admin khong con hoat dong");
            }
            return new AuthTokens(
                    buildToken(user, ACCESS_TOKEN_EXPIRATION, Role.ADMIN.name(), ACCESS_TOKEN_TYPE),
                    buildToken(user, REFRESH_TOKEN_EXPIRATION, Role.ADMIN.name(), REFRESH_TOKEN_TYPE)
            );
        }

        Map<String, Object> user = userClient.getCustomerByEmail(email, true);
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("Tai khoan khach hang khong con hoat dong");
        }
        return new AuthTokens(
                buildCustomerToken(user, ACCESS_TOKEN_EXPIRATION, ACCESS_TOKEN_TYPE),
                buildCustomerToken(user, REFRESH_TOKEN_EXPIRATION, REFRESH_TOKEN_TYPE)
        );
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

    private String buildToken(Map<String, Object> user, long expirationMillis, String role, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.get("email"));
        claims.put("userId", user.get("id"));
        claims.put("fullName", user.get("name"));
        claims.put("pictureUrl", user.get("avatar"));
        claims.put("role", role);
        claims.put("roles", List.of(role));
        claims.put("tokenType", tokenType);

        return buildToken(String.valueOf(user.get("email")), claims, expirationMillis);
    }

    private String buildCustomerToken(Map<String, Object> user, long expirationMillis, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.get("email"));
        claims.put("userId", user.get("id"));
        claims.put("fullName", user.get("name"));
        claims.put("pictureUrl", user.get("avatar"));

        List<String> roles = new ArrayList<>();
        roles.add(Role.USERS.name());
        claims.put("role", Role.USERS.name());
        claims.put("tokenType", tokenType);

        try {
            Map<String, Object> seller = sellerClient.getApprovedSellerByOwner(String.valueOf(user.get("id")));
            if (seller != null && !seller.isEmpty()) {
                roles.add("SELLER");
                claims.put("sellerId", seller.get("id"));
                claims.put("sellerStatus", seller.get("status"));
                claims.put("sellerSlug", seller.get("sellerSlug"));
                claims.put("shopName", seller.get("shopName"));
            }
        } catch (Exception ignored) {
            // Seller role enrichment must not break buyer login when seller-service is temporarily unavailable.
        }

        claims.put("roles", roles);
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
