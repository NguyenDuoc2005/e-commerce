package com.ecommerce.auth.repository;

import com.ecommerce.auth.constant.EntityStatus;
import com.ecommerce.auth.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NhanVienAuthRepository extends JpaRepository<NhanVien, String> {

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByEmailAndStatus(String email, EntityStatus status);
}
