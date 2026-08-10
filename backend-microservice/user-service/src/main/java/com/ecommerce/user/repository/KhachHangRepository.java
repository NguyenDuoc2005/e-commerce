package com.ecommerce.user.repository;

import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    @Query("""
            SELECT kh
            FROM KhachHang kh
            WHERE (:q IS NULL OR :q = '' OR
                   LOWER(kh.ma) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(kh.ten) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:status IS NULL OR kh.status = :status)
            """)
    Page<KhachHang> getAllKhachHang(Pageable pageable, @Param("q") String q, @Param("status") EntityStatus status);

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findByEmailAndStatus(String email, EntityStatus status);

    Optional<KhachHang> findBySdt(String sdt);
}
