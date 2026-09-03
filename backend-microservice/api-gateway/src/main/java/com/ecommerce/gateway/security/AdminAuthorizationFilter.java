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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;
import com.ecommerce.common.security.InternalServiceTokenInterceptor;
import com.ecommerce.common.security.TrustedRequestFilter;

@Component
public class AdminAuthorizationFilter implements WebFilter {

    private static final String ADMIN_PREFIX = "/api/v1/admin/";
    private static final String SELLER_PREFIX = "/api/v1/seller/";
    private static final String BUYER_PREFIX = "/api/v1/buyer/";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SELLER_ROLE = "SELLER";
    private static final String BUYER_ROLE = "USERS";

    @Value("${jwt.secret}")
    private String tokenSecret;

    @Value("${security.gateway-token}")
    private String gatewayToken;

    private final WebClient sellerClient;

    public AdminAuthorizationFilter(WebClient.Builder loadBalancedWebClientBuilder) {
        this.sellerClient = loadBalancedWebClientBuilder.build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        boolean adminRoute = requiresAdmin(exchange);
        boolean sellerRoute = requiresSeller(exchange);
        boolean buyerRoute = requiresBuyer(exchange);
        if (!adminRoute && !sellerRoute && !buyerRoute) {
            return chain.filter(mutateWithMarketplaceHeaders(exchange, null));
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
            if (!"ACCESS".equals(claims.get("tokenType", String.class))) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
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
            if (sellerRoute) {
                return verifySellerIsActive(claims.get("sellerId", String.class), exchange)
                        .flatMap(active -> {
                            if (!active) {
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            }
                            return chain.filter(mutatedExchange);
                        })
                        .onErrorResume(ex -> {
                            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                            return exchange.getResponse().setComplete();
                        });
            }
            return chain.filter(mutatedExchange);
        } catch (JwtException | IllegalArgumentException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private Mono<Boolean> verifySellerIsActive(String sellerId, ServerWebExchange exchange) {
        return sellerClient.get()
                .uri("http://seller-service/internal/sellers/{sellerId}/public", sellerId)
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .map(profile -> !profile.isEmpty())
                .defaultIfEmpty(false);
    }

    private boolean requiresAdmin(ServerWebExchange exchange) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return false;
        }
        String path = exchange.getRequest().getURI().getPath();
        return path.equals("/api/v1/admin") || path.startsWith(ADMIN_PREFIX)
                || path.equals("/api/v1/notifications/email");
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
        return path.equals("/api/v1/buyer") || path.startsWith(BUYER_PREFIX)
                || (path.startsWith("/api/orders/") && !path.equals("/api/orders/vnpay-return"));
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
        String userId = claims == null ? null : claims.get("userId", String.class);
        String sellerId = claims == null ? null : claims.get("sellerId", String.class);
        return exchange.mutate()
                .request(builder -> {
                    builder.headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-Seller-Id");
                        headers.remove(TrustedRequestFilter.GATEWAY_HEADER);
                        headers.remove(InternalServiceTokenInterceptor.HEADER);
                    });
                    builder.header(TrustedRequestFilter.GATEWAY_HEADER, gatewayToken);
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
