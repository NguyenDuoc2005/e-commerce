package com.ecommerce.user.repository;

import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, String> {

    Page<Staff> findByCodeContainingOrNameContaining(String code, String name, Pageable pageable);

    @Query("""
            SELECT nv
            FROM Staff nv
            WHERE (:q IS NULL OR :q = '' OR
                   LOWER(nv.code) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(nv.name) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:status IS NULL OR nv.status = :status)
            """)
    Page<Staff> getAllStaff(Pageable pageable, @Param("q") String q, @Param("status") EntityStatus status);

    boolean existsByIdentityNumberAndIdNot(String identityNumber, String id);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String id);

    boolean existsByEmailAndIdNot(String email, String id);

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findByEmailAndStatus(String email, EntityStatus status);
}
