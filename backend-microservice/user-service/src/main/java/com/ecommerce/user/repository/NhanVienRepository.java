package com.ecommerce.user.repository;

import com.ecommerce.user.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhanVienRepository extends JpaRepository<NhanVien, String> {

    Page<NhanVien> findByMaContainingOrTenContaining(String ma, String ten, Pageable pageable);

    boolean existsByCccdAndIdNot(String cccd, String id);

    boolean existsBySdtAndIdNot(String sdt, String id);

    boolean existsByEmailAndIdNot(String email, String id);
}
