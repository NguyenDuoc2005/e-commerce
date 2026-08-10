package com.ecommerce.user.repository;

import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, String> {

    Page<NhanVien> findByMaContainingOrTenContaining(String ma, String ten, Pageable pageable);

    @Query("""
            SELECT nv
            FROM NhanVien nv
            WHERE (:q IS NULL OR :q = '' OR
                   LOWER(nv.ma) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(nv.ten) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:status IS NULL OR nv.status = :status)
            """)
    Page<NhanVien> getAllNhanVien(Pageable pageable, @Param("q") String q, @Param("status") EntityStatus status);

    boolean existsByCccdAndIdNot(String cccd, String id);

    boolean existsBySdtAndIdNot(String sdt, String id);

    boolean existsByEmailAndIdNot(String email, String id);

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByEmailAndStatus(String email, EntityStatus status);
}
