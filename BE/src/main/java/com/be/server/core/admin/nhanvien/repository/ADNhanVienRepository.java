package com.be.server.core.admin.nhanvien.repository;

import com.be.server.entity.NhanVien;
import com.be.server.repository.NhanVienRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ADNhanVienRepository extends NhanVienRepository {

    Page<NhanVien> findByMaContainingOrTenContaining(String ma, String ten, Pageable pageable);

    boolean existsByCccd(String cccd);
    boolean existsBySdt(String sdt);
    boolean existsByEmail(String email);

    boolean existsByCccdAndIdNot(String cccd, String id);
    boolean existsBySdtAndIdNot(String sdt, String id);
    boolean existsByEmailAndIdNot(String email, String id);

}
