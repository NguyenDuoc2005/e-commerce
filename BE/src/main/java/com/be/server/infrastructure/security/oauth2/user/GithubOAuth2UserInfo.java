package com.be.server.infrastructure.security.oauth2.user;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    private String email;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    public GithubOAuth2UserInfo(Map<String, Object> attributes, String email) {
        super(attributes);
        this.email = email;
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {

        if (email != null && !email.isBlank()) {
            return email;
        }
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
