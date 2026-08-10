package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.PhieuGiamGiaChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhieuGiamGiaChiTietRepository extends JpaRepository<PhieuGiamGiaChiTiet, String> {
    @Query("select distinct p.khachHangId from PhieuGiamGiaChiTiet p where p.phieuGiamGia.id = :voucherId")
    List<String> findCustomerIdsByVoucherId(@Param("voucherId") String voucherId);
}
