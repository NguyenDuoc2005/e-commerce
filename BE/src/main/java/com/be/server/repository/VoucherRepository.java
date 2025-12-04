package com.be.server.repository;

import com.be.server.entity.DotGiamGia;
import com.be.server.entity.PhieuGiamGia;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.StatusPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface VoucherRepository extends JpaRepository<PhieuGiamGia, String> {

    // Trong VoucherRepository.java
    @Query("SELECT v FROM PhieuGiamGia v WHERE v.ngayBatDau IS NOT NULL AND v.ngayKetThuc IS NOT NULL")
    List<PhieuGiamGia> findVouchersNeedStatusUpdate();;

}
