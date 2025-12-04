package com.be.server.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppConfig {

    @Value("${GENERATE_DB}")
    private boolean generateDb;

    @Value("${USER_EMAIL}")
    private String userEmail;

    @Value("${USER_CODE}")
    private String userCode;

    @Value("${USER_NAME}")
    private String userName;

    @Value("${USER_PHONE}")
    private String userPhone;

    @Value("${ROLE}")
    private String role;
}
