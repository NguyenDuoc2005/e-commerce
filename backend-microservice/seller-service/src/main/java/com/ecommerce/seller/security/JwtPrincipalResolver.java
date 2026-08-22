package com.ecommerce.seller.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtPrincipalResolver {

    private static final String BEARER = "Bearer ";

    @Value("${jwt.secret}")
    private String tokenSecret;

    public String customerId(HttpServletRequest request) {
        Claims claims = claims(request);
        String userId = claims.get("userId", String.class);
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Token khong co userId");
        }
        return userId;
    }

    public String staffId(HttpServletRequest request) {
        Claims claims = claims(request);
        return claims.get("userId", String.class);
    }

    public String sellerId(HttpServletRequest request) {
        String sellerId = request.getHeader("X-Seller-Id");
        if (sellerId != null && !sellerId.isBlank()) {
            return sellerId;
        }
        Claims claims = claims(request);
        sellerId = claims.get("sellerId", String.class);
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Token khong co sellerId");
        }
        return sellerId;
    }

    private Claims claims(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER)) {
            throw new IllegalArgumentException("Thieu Authorization Bearer token");
        }
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(authorization.substring(BEARER.length()))
                .getBody();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(tokenSecret.getBytes());
    }
}
