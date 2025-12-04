package com.be.server.infrastructure.security.oauth2;

import com.be.server.entity.KhachHang;
import com.be.server.infrastructure.constant.AuthProvider;
import com.be.server.infrastructure.constant.CookieConstant;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.OAuth2Constant;
import com.be.server.infrastructure.exception.OAuth2AuthenticationProcessingException;
import com.be.server.infrastructure.security.oauth2.user.GithubOAuth2UserInfo;
import com.be.server.infrastructure.security.oauth2.user.OAuth2UserInfo;
import com.be.server.infrastructure.security.oauth2.user.OAuth2UserInfoFactory;
import com.be.server.infrastructure.security.repository.KhachHangAuthRepository;
import com.be.server.infrastructure.security.user.UserPrincipal;
import com.be.server.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final KhachHangAuthRepository khachHangAuthRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes()
        );

        if (AuthProvider.github.toString().equals(oAuth2UserRequest.getClientRegistration().getRegistrationId())
                && (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isBlank())) {
            String email = fetchGithubEmail(oAuth2UserRequest);
            if (email != null) {
                oAuth2UserInfo = new GithubOAuth2UserInfo(oAuth2User.getAttributes(), email);
            }
        }

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isBlank()) {
            CookieUtils.addCookie(httpServletResponse, CookieConstant.ACCOUNT_NOT_EXIST, CookieConstant.ACCOUNT_NOT_EXIST);
            throw new OAuth2AuthenticationProcessingException(CookieConstant.ACCOUNT_NOT_EXIST);
        }

        Optional<Cookie> cookieOpRole = CookieUtils.getCookie(httpServletRequest, OAuth2Constant.SCREEN_FOR_ROLE_COOKIE_NAME);
        Optional<Cookie> cookieRegister = CookieUtils.getCookie(httpServletRequest, OAuth2Constant.REGISTER_PARAM_COOKIE_NAME);

        if (cookieOpRole.isPresent()) {
            String registerValue = cookieRegister.map(Cookie::getValue).orElse("false");
            boolean isRegister = "true".equalsIgnoreCase(registerValue);
            String role = cookieOpRole.get().getValue();
            log.info("Received role: {}", role);

            if (OAuth2Constant.ROLE_ADMIN.equals(role)) {
                return processAdmin(oAuth2UserInfo, role, isRegister);
            }
            if (OAuth2Constant.ROLE_USER.equals(role)) {
                return processUser(oAuth2UserInfo, role, isRegister);
            }
        }

        CookieUtils.addCookie(httpServletResponse, CookieConstant.ACCOUNT_NOT_EXIST, CookieConstant.ACCOUNT_NOT_EXIST);
        throw new OAuth2AuthenticationProcessingException(CookieConstant.ACCOUNT_NOT_EXIST);
    }

    private OAuth2User processAdmin(OAuth2UserInfo oAuth2UserInfo, String role, boolean register) {
        // TODO: Implement logic to process ADMIN user
        return null;
    }

    private OAuth2User processUser(OAuth2UserInfo oAuth2UserInfo, String role, boolean register) {
        Optional<KhachHang> userOptional = khachHangAuthRepository.findByEmailAndStatus(
                oAuth2UserInfo.getEmail(), EntityStatus.ACTIVE
        );

        if (userOptional.isPresent()) {
            KhachHang khachHang = userOptional.get();

            // Cập nhật thông tin mới từ OAuth2 provider
            khachHang.setEmail(oAuth2UserInfo.getEmail());
            khachHang.setAvatar(oAuth2UserInfo.getImageUrl());

            khachHangAuthRepository.save(khachHang);

            //  Gán role = USERS và tạo UserPrincipal có authority tương ứng
            UserPrincipal principal = UserPrincipal.createFromKhachHang(khachHang);
            principal.setAttributes(oAuth2UserInfo.getAttributes());
            return principal;

        } else {
            // Nếu chưa có tài khoản thì lưu cookie và throw lỗi
            CookieUtils.addCookie(httpServletResponse, CookieConstant.ACCOUNT_NOT_EXIST, CookieConstant.ACCOUNT_NOT_EXIST);
            throw new OAuth2AuthenticationProcessingException(CookieConstant.ACCOUNT_NOT_EXIST);
        }
    }

    private String fetchGithubEmail(OAuth2UserRequest userRequest) {
        String token = userRequest.getAccessToken().getTokenValue();
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + token);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

        List<Map<String, Object>> emails = response.getBody();
        if (emails != null) {
            for (Map<String, Object> e : emails) {
                Boolean primary = (Boolean) e.get("primary");
                Boolean verified = (Boolean) e.get("verified");
                if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                    return (String) e.get("email");
                }
            }
        }
        return null;
    }
}