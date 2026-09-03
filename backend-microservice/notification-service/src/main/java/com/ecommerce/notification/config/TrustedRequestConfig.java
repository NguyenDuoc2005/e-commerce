package com.ecommerce.notification.config;

import com.ecommerce.common.security.TrustedRequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class TrustedRequestConfig {
    @Bean
    public FilterRegistrationBean<TrustedRequestFilter> trustedRequestFilter(
            @Value("${security.gateway-token}") String gatewayToken,
            @Value("${security.internal-service-token}") String internalToken) {
        FilterRegistrationBean<TrustedRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TrustedRequestFilter(gatewayToken, internalToken));
        registration.addUrlPatterns("/internal/*", "/api/v1/notifications/email");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
