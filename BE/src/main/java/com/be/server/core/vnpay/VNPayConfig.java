package com.be.server.core.vnpay;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class VNPayConfig {
    @Value("${vnpay.tmnCode}")
    public String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    public String vnp_HashSecret;

    @Value("${vnpay.url}")
    public String vnp_PayUrl;

    @Value("http://localhost:8386/api/orders/vnpay-return")
    public String vnp_ReturnUrl;
}