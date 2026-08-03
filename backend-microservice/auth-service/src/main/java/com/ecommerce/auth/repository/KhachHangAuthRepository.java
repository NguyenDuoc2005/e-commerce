package com.ecommerce.auth.repository;

import com.ecommerce.auth.constant.EntityStatus;
import com.ecommerce.auth.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhachHangAuthRepository extends JpaRepository<KhachHang, String> {

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findByEmailAndStatus(String email, EntityStatus status);

    Optional<KhachHang> findBySdt(String sdt);
}
