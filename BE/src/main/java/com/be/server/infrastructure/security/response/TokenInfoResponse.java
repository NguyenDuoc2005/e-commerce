package com.be.server.infrastructure.security.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TokenInfoResponse {

    private String userId;

    private String userCode;

    private String userName;

    private String pictureUrl;

    private List<String> rolesCode;

    private List<String> rolesName;

    private String email;

    private String roleScreen ;

}
