package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.request.ChangePasswordRequest;
import com.ecommerce.auth.dto.request.LoginRequest;
import com.ecommerce.auth.dto.request.RegisterRequest;
import com.ecommerce.auth.security.LoginRoleContext;
import com.ecommerce.auth.security.TokenProvider;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.common.base.AuthTokens;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.ResponseUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final TokenProvider tokenProvider;

    private final AuthService authService;

    public AuthController(
            AuthenticationManager authenticationManager,
            TokenProvider tokenProvider,
            AuthService authService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession httpSession) {
        return authenticate(loginRequest, httpSession, "USER");
    }

    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest loginRequest, HttpSession httpSession) {
        return authenticate(loginRequest, httpSession, "ADMIN");
    }

    @PutMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseUtils.createResponseEntity(authService.register(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpSession session
    ) {
        try {
            String email = (String) session.getAttribute("email");
            if (email == null && authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                if (tokenProvider.validateToken(token)) {
                    email = tokenProvider.getEmailFromToken(token);
                }
            }
            if (email == null) {
                return ResponseUtils.createResponseEntity(
                        new ResponseObject<>(null, HttpStatus.UNAUTHORIZED, "Chua dang nhap hoac phien da het han")
                );
            }

            return ResponseUtils.createResponseEntity(authService.changePassword(email, request));
        } catch (IllegalArgumentException ex) {
            return ResponseUtils.createResponseEntity(new ResponseObject<>(null, HttpStatus.BAD_REQUEST, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseUtils.createResponseEntity(new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Loi he thong: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()
            ));
        }
    }

    private ResponseEntity<?> authenticate(LoginRequest loginRequest, HttpSession httpSession, String role) {
        try {
            httpSession.setAttribute("role", role);
            LoginRoleContext.set(role);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            String accessToken = "ADMIN".equals(role)
                    ? tokenProvider.createTokenForAdmin(authentication)
                    : tokenProvider.createTokenForCustomer(authentication);
            String refreshToken = "ADMIN".equals(role)
                    ? tokenProvider.createRefreshTokenForAdmin(authentication)
                    : tokenProvider.createRefreshTokenForCustomer(authentication);

            return ResponseUtils.createResponseEntity(
                    new ResponseObject<>(new AuthTokens(accessToken, refreshToken), HttpStatus.OK, "Lay token thanh cong")
            );
        } catch (BadCredentialsException ex) {
            return ResponseUtils.createResponseEntity(
                    new ResponseObject<>(null, HttpStatus.UNAUTHORIZED, "Email hoac mat khau khong dung")
            );
        } catch (DisabledException ex) {
            return ResponseUtils.createResponseEntity(
                    new ResponseObject<>(null, HttpStatus.UNAUTHORIZED, "Tai khoan da bi vo hieu hoa")
            );
        } catch (LockedException ex) {
            return ResponseUtils.createResponseEntity(
                    new ResponseObject<>(null, HttpStatus.UNAUTHORIZED, "Tai khoan da bi khoa")
            );
        } catch (CredentialsExpiredException ex) {
            return ResponseUtils.createResponseEntity(
                    new ResponseObject<>(null, HttpStatus.UNAUTHORIZED, "Thong tin wardc thuc da het han")
            );
        } catch (Exception ex) {
            return ResponseUtils.createResponseEntity(new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Loi he thong: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()
            ));
        } finally {
            LoginRoleContext.clear();
        }
    }
}
