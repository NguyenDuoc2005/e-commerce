package com.be.server.infrastructure.security.service;

import com.be.server.entity.RefreshToken;
import com.be.server.infrastructure.security.repository.RefreshTokenAuthRepository;
import com.be.server.infrastructure.security.repository.KhachHangAuthRepository;
import com.be.server.infrastructure.security.user.UserPrincipal;
import com.be.server.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
@Service
public class RefreshTokenService {

    private final long REFRESH_EXPIRED_TIME = 6 * 60 * 60 * 1000;

    private final RefreshTokenAuthRepository refreshTokenRepository;

    private final KhachHangAuthRepository userAuthRepository;

    @Autowired
    public RefreshTokenService(
            RefreshTokenAuthRepository refreshTokenRepository,
            KhachHangAuthRepository staffRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAuthRepository = staffRepository;
    }

    public Optional<RefreshToken> findByToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    public RefreshToken createRefreshToken(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

//        Optional<User> staff = userAuthRepository.findByEmail(userPrincipal.getEmail());
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(userPrincipal.getId());

        if (optionalRefreshToken.isPresent()) {
            if (optionalRefreshToken.get().getRevokedAt() != null) {
                optionalRefreshToken.get().setRevokedAt(null);
                optionalRefreshToken.get().setExpiredAt(REFRESH_EXPIRED_TIME);
                optionalRefreshToken.get().setRefreshToken(UUID.randomUUID().toString());
                return refreshTokenRepository.save(optionalRefreshToken.get());
            }
            RefreshToken refreshToken = optionalRefreshToken.get();
            refreshToken.setExpiredAt(REFRESH_EXPIRED_TIME);
            refreshToken.setRefreshToken(UUID.randomUUID().toString());
            refreshToken = refreshTokenRepository.save(refreshToken);
            return refreshToken;
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userPrincipal.getId());
        refreshToken.setExpiredAt(REFRESH_EXPIRED_TIME);
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken createRefreshToken(String userId) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(userId);
        if (optionalRefreshToken.isPresent()) {
            if (optionalRefreshToken.get().getRevokedAt() != null) {
                optionalRefreshToken.get().setRevokedAt(null);
                optionalRefreshToken.get().setExpiredAt(REFRESH_EXPIRED_TIME);
                optionalRefreshToken.get().setRefreshToken(UUID.randomUUID().toString());
                return refreshTokenRepository.save(optionalRefreshToken.get());
            }
            RefreshToken refreshToken = optionalRefreshToken.get();
            refreshToken.setExpiredAt(REFRESH_EXPIRED_TIME);
            refreshToken.setRefreshToken(UUID.randomUUID().toString());
            return refreshTokenRepository.save(refreshToken);
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setExpiredAt(REFRESH_EXPIRED_TIME);
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiredAt() < DateTimeUtil.convertDateToTimeStampSecond(new Date())) {
            refreshTokenRepository.delete(token);
            return null;
        }
        return token;
    }


}
