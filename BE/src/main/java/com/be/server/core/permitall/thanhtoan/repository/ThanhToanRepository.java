package com.be.server.core.permitall.thanhtoan.repository;

import com.be.server.entity.LichSuTrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThanhToanRepository extends JpaRepository<LichSuTrangThaiHoaDon, String> {
}
