package com.be.server.core.permitall.profile.repository;

import com.be.server.core.permitall.profile.model.PMKhachHangResponse;
import com.be.server.repository.KhachHangRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PMProfileRepository extends KhachHangRepository {

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
