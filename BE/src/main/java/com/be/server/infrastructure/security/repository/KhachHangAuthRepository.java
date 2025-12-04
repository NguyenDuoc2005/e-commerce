package com.be.server.infrastructure.security.repository;

import com.be.server.entity.KhachHang;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.KhachHangRepository;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public interface KhachHangAuthRepository extends KhachHangRepository {

    Optional<KhachHang> findByEmail(@Size(max = 255) String email);

    Optional<KhachHang> findByEmailAndStatus(@Size(max = 255) String email, EntityStatus status);

}
