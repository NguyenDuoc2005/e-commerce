package com.ecommerce.user.repository;

import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    @Query("""
            SELECT kh
            FROM Customer kh
            WHERE (:q IS NULL OR :q = '' OR
                   LOWER(kh.code) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(kh.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(kh.phoneNumber) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:status IS NULL OR kh.status = :status)
            """)
    Page<Customer> getAllCustomer(Pageable pageable, @Param("q") String q, @Param("status") EntityStatus status);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByEmailAndStatus(String email, EntityStatus status);

    Optional<Customer> findByPhoneNumber(String phoneNumber);
}
