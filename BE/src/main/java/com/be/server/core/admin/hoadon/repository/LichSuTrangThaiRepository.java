package com.be.server.core.admin.hoadon.repository;

import com.be.server.core.admin.hoadon.model.response.TrangThaiThoiGianResponse;
import com.be.server.entity.LichSuTrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuTrangThaiRepository extends JpaRepository<LichSuTrangThaiHoaDon, Long> {

    @Query(value = "SELECT trang_thai AS trangThai, thoi_gian AS thoiGian, note as note FROM lich_su_trang_thai_hoa_don WHERE hoa_don_id = :hoaDonId", nativeQuery = true)
    List<TrangThaiThoiGianResponse> findTrangThaiVaThoiGianByHoaDonId(@Param("hoaDonId") String hoaDonId);

}