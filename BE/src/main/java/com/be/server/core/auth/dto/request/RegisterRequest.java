package com.be.server.core.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String userName;

    private String email;

    private String phone;

    private String password;

}
