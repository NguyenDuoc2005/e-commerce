package com.be.server.core.admin.hoadon.repository;

import com.be.server.core.admin.hoadon.model.response.ADHoaDonChiTietResponseDetail;
import com.be.server.repository.HoaDonChiTietRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADHoaDonChiTietRepository extends HoaDonChiTietRepository {

    @Query(value = """
                SELECT
                    hd.ma_hoa_don AS maHoaDon,
                    hd.ten_hoa_don AS tenHoaDon,
                    hdct.ma_hoa_don_chi_tiet AS maHoaDonChiTiet,
                    sp.ten_san_pham AS tenSanPham,
                    spct.anh_san_pham AS anhSanPham,
                    thuong_hieu.ten_thuong_hieu AS thuongHieu,
                    mau_sac.ten_mau_sac AS mauSac,
                    kich_co.ten_kich_co AS size,
                    hdct.so_luong AS soLuong,
                    hdct.gia_ban AS giaBan,
                    (
                        SELECT SUM(hdsub.so_luong * hdsub.gia_ban)
                        FROM hoa_don_chi_tiet hdsub
                        JOIN san_pham_chi_tiet spctsub ON spctsub.id = hdsub.id_spct
                        WHERE hdsub.id_hoa_don = hd.id
                    ) AS thanhTien,
                    kh.ten_khach_hang AS tenKhachHang2,
                    kh.so_dien_thoai AS sdtKH2,
                    kh.email AS email2,
                    kh.dia_chi AS diaChi2,                               
                    hd.ten_khach_hang AS tenKhachHang,
                    hd.so_dien_thoai_khach_hang AS sdtKH,
                    hd.email AS email,
                    hd.dia_chi_giao_hang AS diaChi,
                    hd.loai_hoa_don AS loaiHoaDon,
                    hd.trang_thai_hoa_don AS trangThaiHoaDon,
                    hd.created_date AS ngayTao,
                    hd.phi_van_chuyen AS phiVanChuyen,
                    pgg.ma_phieu_giam_gia AS maVoucher,
                    pgg.ten_phieu_giam_gia AS tenVoucher,
                    hd.giam_gia AS giaTriVoucher,
                    hd.tong_tien_sau_giam AS tongTienSauGiam,
                    (hdct.gia_ban * hdct.so_luong ) AS tongTien,
                    hd.phuong_thuc_thanh_toan AS phuongThucThanhToan
                FROM hoa_don  hd
                LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id
                LEFT JOIN phieu_giam_gia pgg ON hd.id_voucher = pgg.id
                LEFT JOIN khach_hang kh ON hd.id_khach_hang = kh.id
                LEFT JOIN san_pham_chi_tiet spct ON hdct.id_spct = spct.id
                LEFT JOIN san_pham sp ON spct.id_san_pham = sp.id
                LEFT JOIN thuong_hieu ON sp.id_thuong_hieu = thuong_hieu.id
                LEFT JOIN kich_co ON spct.id_kich_co = kich_co.id
                LEFT JOIN mau_sac ON spct.id_mau_sac = mau_sac.id
                WHERE hd.ma_hoa_don = :maHoaDon
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM hoa_don_chi_tiet hdct
                    LEFT JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
                    WHERE hd.ma_hoa_don = :maHoaDon
                    """,
            nativeQuery = true)
    Page<ADHoaDonChiTietResponseDetail> getAllHoaDonChiTietResponse(@Param("maHoaDon") String maHoaDon, Pageable pageable);

    @Query(value = """
                SELECT
                    hd.ma_hoa_don AS maHoaDon,
                    hd.ten_hoa_don AS tenHoaDon,
                    hdct.ma_hoa_don_chi_tiet AS maHoaDonChiTiet,
                    sp.ten_san_pham AS tenSanPham,
                    spct.anh_san_pham AS anhSanPham,
                    thuong_hieu.ten_thuong_hieu AS thuongHieu,
                    mau_sac.ten_mau_sac AS mauSac,
                    kich_co.ten_kich_co AS size,
                    hdct.so_luong AS soLuong,
                    hdct.gia_ban AS giaBan,
                    (
                        SELECT SUM(hdsub.so_luong * hdsub.gia_ban)
                        FROM hoa_don_chi_tiet hdsub
                        JOIN san_pham_chi_tiet spctsub ON spctsub.id = hdsub.id_spct
                        WHERE hdsub.id_hoa_don = hd.id
                    ) AS thanhTien,
                    hd.ten_khach_hang AS tenKhachHang,
                    hd.so_dien_thoai_khach_hang AS sdtKH,
                    hd.email AS email,
                    hd.dia_chi_giao_hang AS diaChi,
                    hd.loai_hoa_don AS loaiHoaDon,
                    hd.trang_thai_hoa_don AS trangThaiHoaDon,
                    hd.created_date AS ngayTao,
                    hd.phi_van_chuyen AS phiVanChuyen,
                    pgg.ma_phieu_giam_gia AS maVoucher,
                    pgg.ten_phieu_giam_gia AS tenVoucher,
                    pgg.gia_giam_toi_da AS giaTriVoucher,
                    hd.tong_tien_sau_giam AS tongTienSauGiam,
                    (hdct.gia_ban * hdct.so_luong ) AS tongTien,
                    hd.phuong_thuc_thanh_toan as phuongThucThanhToan,
                    hd.du_no AS duNo,
                    hd.hoan_phi AS hoanPhi
                FROM hoa_don_chi_tiet hdct
                LEFT JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
                LEFT JOIN phieu_giam_gia pgg ON hd.id_voucher = pgg.id
                LEFT JOIN khach_hang kh ON hd.id_khach_hang = kh.id
                LEFT JOIN san_pham_chi_tiet spct ON hdct.id_spct = spct.id
                LEFT JOIN san_pham sp ON spct.id_san_pham = sp.id
                LEFT JOIN thuong_hieu ON sp.id_thuong_hieu = thuong_hieu.id
                LEFT JOIN kich_co ON spct.id_kich_co = kich_co.id
                LEFT JOIN mau_sac ON spct.id_mau_sac = mau_sac.id
                WHERE hd.ma_hoa_don = :maHoaDon
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM hoa_don_chi_tiet hdct
                    LEFT JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
                    WHERE hd.ma_hoa_don = :maHoaDon
                    """,
            nativeQuery = true)
    List<ADHoaDonChiTietResponseDetail> getAllHoaDonChiTietResponse2(@Param("maHoaDon") String maHoaDon);

    @Query(value = """
            SELECT
                                                                        hd.ma_hoa_don AS maHoaDon,
                                                                        hd.ten_hoa_don AS tenHoaDon,
                                                                        hdct.ma_hoa_don_chi_tiet AS maHoaDonChiTiet,
                                                                        sp.ten_san_pham AS tenSanPham,
                                                                        spct.anh_san_pham AS anhSanPham,
                                                                        thuong_hieu.ten_thuong_hieu AS thuongHieu,
                                                                        mau_sac.ten_mau_sac AS mauSac,
                                                                        kich_co.ten_kich_co AS size,
                                                                        hdct.so_luong AS soLuong,
                                                                        spct.gia_ban AS giaBan,
                                                                        (hdct.so_luong * spct.gia_ban) AS thanhTienSP,
                                                                        (SELECT SUM(hdct.so_luong * sp.gia_ban) FROM hoa_don_chi_tiet hdct JOIN san_pham_chi_tiet sp ON sp.id = hdct.id_spct  WHERE hdct.id_hoa_don = hd.id) AS thanhTien,
                                                                        kh.ten_khach_hang AS tenKhachHang,
                                                                        kh.so_dien_thoai AS sdtKH,
                                                                        kh.email AS email,
                                                                        kh.dia_chi AS diaChi,
                                                                        hd.loai_hoa_don AS loaiHoaDon,
                                                                        hd.trang_thai_hoa_don AS trangThaiHoaDon,
                                                                        hd.created_date AS ngayTao,
                                                                        hd.phi_van_chuyen AS phiVanChuyen,
                                                                        pgg.ma_phieu_giam_gia AS maVoucher,
                                                                        (SELECT SUM(tong_tien_sau_giam) FROM hoa_don WHERE id = hd.id) AS tongTienSauGiam,
                                                                        (SELECT SUM(gia_ban) FROM hoa_don_chi_tiet WHERE id_hoa_don = hd.id) AS tongTien
                                                                    FROM hoa_don_chi_tiet hdct
                                                                    LEFT JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
                                                                    LEFT JOIN phieu_giam_gia pgg ON hd.id_voucher = pgg.id
                                                                    LEFT JOIN khach_hang kh ON hd.id_khach_hang = kh.id
                                                                    LEFT JOIN san_pham_chi_tiet spct ON hdct.id_spct = spct.id
                                                                    LEFT JOIN san_pham sp ON spct.id_san_pham = sp.id
                                                                    LEFT JOIN thuong_hieu ON sp.id_thuong_hieu = thuong_hieu.id
                                                                    LEFT JOIN kich_co ON spct.id_kich_co = kich_co.id
                                                                    LEFT JOIN mau_sac ON spct.id_mau_sac = mau_sac.id
                                                                    WHERE hd.ma_hoa_don = :maHoaDon
            """, nativeQuery = true)
    List<ADHoaDonChiTietResponseDetail> getAllHoaDonChiTietResponse1(@Param("maHoaDon") String maHoaDon);
}