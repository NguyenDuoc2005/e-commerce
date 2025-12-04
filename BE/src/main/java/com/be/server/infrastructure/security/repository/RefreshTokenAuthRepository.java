package com.be.server.infrastructure.security.repository;

import com.be.server.entity.RefreshToken;
import com.be.server.repository.RefreshTokenRepository;

import java.util.Optional;

public interface RefreshTokenAuthRepository extends RefreshTokenRepository {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByUserId(String userId);
}
