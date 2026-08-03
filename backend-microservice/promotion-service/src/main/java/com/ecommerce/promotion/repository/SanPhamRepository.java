package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, String> {
    @Query(value = "SELECT * FROM san_pham WHERE status = 0", nativeQuery = true)
    List<SanPham> findAllSanPham();
}
