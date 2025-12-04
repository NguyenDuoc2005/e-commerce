package com.be.server.core.auth.repository;

import com.be.server.entity.KhachHang;
import com.be.server.repository.KhachHangRepository;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public interface AuthUserRepository extends KhachHangRepository {
    Optional<KhachHang> findByEmail(@Size(max = 255) String email);

    Optional<KhachHang> findBySdt(String sdt);
}
