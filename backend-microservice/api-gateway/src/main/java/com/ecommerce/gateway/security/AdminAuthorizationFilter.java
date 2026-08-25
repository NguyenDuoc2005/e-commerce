package com.ecommerce.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class AdminAuthorizationFilter implements WebFilter {

    private static final String ADMIN_PREFIX = "/api/v1/admin/";
    private static final String SELLER_PREFIX = "/api/v1/seller/";
    private static final String BUYER_PREFIX = "/api/v1/buyer/";
    private static final List<String> BUYER_AUTH_PATHS = List.of(
            "/api/v1/permitall/reviews",
            "/api/v1/permitall/don-mua",
            "/api/v1/permitall/cart"
    );
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SELLER_ROLE = "SELLER";
    private static final String BUYER_ROLE = "USERS";

    @Value("${jwt.secret}")
    private String tokenSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        boolean adminRoute = requiresAdmin(exchange);
        boolean sellerRoute = requiresSeller(exchange);
        boolean buyerRoute = requiresBuyer(exchange);
        if (!adminRoute && !sellerRoute && !buyerRoute) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authorization.substring(BEARER_PREFIX.length()))
                    .getBody();
            if (adminRoute && !hasRole(claims, ADMIN_ROLE)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (sellerRoute && (!hasRole(claims, SELLER_ROLE) || claims.get("sellerId", String.class) == null)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (buyerRoute && !hasRole(claims, BUYER_ROLE)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            ServerWebExchange mutatedExchange = mutateWithMarketplaceHeaders(exchange, claims);
            return chain.filter(mutatedExchange);
        } catch (JwtException | IllegalArgumentException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean requiresAdmin(ServerWebExchange exchange) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return false;
        }
        String path = exchange.getRequest().getURI().getPath();
        return path.equals("/api/v1/admin") || path.startsWith(ADMIN_PREFIX);
    }

    private boolean requiresSeller(ServerWebExchange exchange) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return false;
        }
        String path = exchange.getRequest().getURI().getPath();
        return path.equals("/api/v1/seller") || path.startsWith(SELLER_PREFIX);
    }

    private boolean requiresBuyer(ServerWebExchange exchange) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return false;
        }
        String path = exchange.getRequest().getURI().getPath();
        if (path.equals("/api/v1/buyer") || path.startsWith(BUYER_PREFIX)) {
            return true;
        }
        if (path.matches("/api/v1/permitall/shops/[^/]+/follow")) {
            return true;
        }
        if (path.equals("/api/v1/permitall/reviews")) {
            return HttpMethod.POST.equals(exchange.getRequest().getMethod());
        }
        if (path.equals("/api/v1/permitall/reviews/mine")) {
            return HttpMethod.GET.equals(exchange.getRequest().getMethod());
        }
        return BUYER_AUTH_PATHS.stream()
                .filter(candidate -> !candidate.equals("/api/v1/permitall/reviews"))
                .anyMatch(candidate -> path.equals(candidate) || path.startsWith(candidate + "/"));
    }

    private boolean hasRole(Claims claims, String requiredRole) {
        String role = claims.get("role", String.class);
        if (requiredRole.equals(role)) {
            return true;
        }
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().anyMatch(requiredRole::equals);
        }
        return false;
    }

    private ServerWebExchange mutateWithMarketplaceHeaders(ServerWebExchange exchange, Claims claims) {
        String userId = claims.get("userId", String.class);
        String sellerId = claims.get("sellerId", String.class);
        return exchange.mutate()
                .request(builder -> {
                    if (userId != null) {
                        builder.header("X-User-Id", userId);
                    }
                    if (sellerId != null) {
                        builder.header("X-Seller-Id", sellerId);
                    }
                })
                .build();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(tokenSecret.getBytes());
    }
}
