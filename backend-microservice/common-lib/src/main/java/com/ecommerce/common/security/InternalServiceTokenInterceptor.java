package com.ecommerce.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/** Adds a credential to calls that are allowed to cross service boundaries. */
public final class InternalServiceTokenInterceptor implements RequestInterceptor {
    public static final String HEADER = "X-Internal-Service-Token";

    private final String token;

    public InternalServiceTokenInterceptor(String token) {
        this.token = token;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.removeHeader(HEADER);
        template.header(HEADER, token);
    }
}
