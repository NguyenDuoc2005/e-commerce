package com.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Enforces the network trust boundary at every downstream service. Internal
 * controllers require the service credential, while role-protected marketplace
 * controllers require the credential injected by the API gateway. This prevents
 * callers that can reach a service port from forging X-User-Id/X-Seller-Id.
 */
public final class TrustedRequestFilter extends OncePerRequestFilter {
    public static final String GATEWAY_HEADER = "X-Gateway-Token";

    private final byte[] gatewayToken;
    private final byte[] internalToken;

    public TrustedRequestFilter(String gatewayToken, String internalToken) {
        if (gatewayToken == null || gatewayToken.isBlank() || internalToken == null || internalToken.isBlank()) {
            throw new IllegalStateException("Trusted service credentials must be configured");
        }
        this.gatewayToken = bytes(gatewayToken);
        this.internalToken = bytes(internalToken);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean internalPath = path.startsWith("/internal/");
        boolean notificationEmailPath = path.equals("/api/v1/notifications/email");
        boolean gatewayProtectedPath = protectedPrefix(path, "/api/v1/admin")
                || protectedPrefix(path, "/api/v1/seller")
                || protectedPrefix(path, "/api/v1/buyer")
                || protectedPrefix(path, "/api/orders") && !path.equals("/api/orders/vnpay-return")
                || notificationEmailPath;
        if (!internalPath && !gatewayProtectedPath) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean trustedInternal = matches(request.getHeader(InternalServiceTokenInterceptor.HEADER), internalToken);
        boolean trustedGateway = matches(request.getHeader(GATEWAY_HEADER), gatewayToken);
        boolean trusted = internalPath
                ? trustedInternal
                : notificationEmailPath
                        ? trustedGateway || trustedInternal
                        : trustedGateway;
        if (!trusted) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Trusted service credential required");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean protectedPrefix(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private boolean matches(String supplied, byte[] expected) {
        return supplied != null && MessageDigest.isEqual(bytes(supplied), expected);
    }

    private static byte[] bytes(String value) {
        return (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
    }
}
