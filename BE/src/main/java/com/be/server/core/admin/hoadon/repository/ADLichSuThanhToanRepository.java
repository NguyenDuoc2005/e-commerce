package com.be.server.core.admin.hoadon.repository;

import com.be.server.core.admin.hoadon.model.response.LichSuThanhToanResponse;
import com.be.server.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADLichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Long> {

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY l.thoi_gian DESC) AS stt,
            l.so_tien AS soTien,
            l.thoi_gian AS thoiGian,
            l.ma_giao_dich AS maGiaoDich,
            l.loai_giao_dich AS loaiGiaoDich,
            l.ghi_chu AS ghiChu,
            n.ten_nhan_vien AS tenNhanVien,
            l.hoa_don_id AS hoaDonId
        FROM lich_su_thanh_toan l
        LEFT JOIN nhan_vien n ON l.nhan_vien_id = n.id
        WHERE l.hoa_don_id = :hoaDonId
        ORDER BY l.thoi_gian DESC
    """, nativeQuery = true)
    List<LichSuThanhToanResponse> getLichSuThanhToanByHoaDonId(@Param("hoaDonId") String hoaDonId);

}
