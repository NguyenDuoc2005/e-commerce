package com.ecommerce.user.repository;

import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.entity.KhachHang;
import com.ecommerce.user.model.response.PMKhachHangResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    @Query("""
            SELECT kh
            FROM KhachHang kh
            WHERE (:q IS NULL OR :q = '' OR
                   LOWER(kh.ma) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(kh.ten) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:status IS NULL OR kh.status = :status)
            """)
    Page<KhachHang> getAllKhachHang(Pageable pageable, @Param("q") String q, @Param("status") EntityStatus status);

    @Query(value = """
            SELECT
                hd.id AS id,
                hd.ma_hoa_don AS ma,
                hd.ten_hoa_don AS ten,
                kh.so_dien_thoai AS sdt,
                kh.ten_khach_hang AS tenKH,
                hd.phi_van_chuyen AS phiVanChuyen,
                kh.dia_chi AS diaChi,
                hd.tong_tien_sau_giam AS tongTienSauGiam,
                hd.tong_tien AS tongTien,
                hd.ghi_chu AS ghiChu,
                hd.phuong_thuc_thanh_toan AS phuongThucThanhToan,
                hd.loai_hoa_don AS loaiHoaDon,
                hd.trang_thai_hoa_don AS trangThaiHoaDon,
                hd.created_date AS ngayTao
            FROM khach_hang kh
            LEFT JOIN hoa_don hd ON kh.id = hd.id_khach_hang
            LEFT JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don
            WHERE kh.id = :id AND hd.loai_hoa_don = '2'
            """, nativeQuery = true)
    List<PMKhachHangResponse> getLSKH(@Param("id") String id);
}
