package com.be.server.core.admin.hoadon.repository;

import com.be.server.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ADHoaDonRepository extends JpaRepository<HoaDon, String>, ADHoaDonRepositoryCustom {

    @Query("SELECT h FROM HoaDon h WHERE h.ma = :ma")
    Optional<HoaDon> findByMa(@Param("ma") String ma);
}
