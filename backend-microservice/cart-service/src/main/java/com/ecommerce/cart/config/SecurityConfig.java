package com.ecommerce.cart.config;

import com.ecommerce.common.security.InternalServiceTokenInterceptor;
import com.ecommerce.common.security.TrustedRequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${security.gateway-token}") String gatewayToken,
            @Value("${security.internal-service-token}") String internalToken) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        http.addFilterBefore(new TrustedRequestFilter(gatewayToken, internalToken), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public InternalServiceTokenInterceptor internalServiceTokenInterceptor(
            @Value("${security.internal-service-token}") String token) {
        return new InternalServiceTokenInterceptor(token);
    }
}
