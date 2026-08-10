package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.DotGiamGia;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;
import com.ecommerce.promotion.model.response.PromotionByProductDetail;
import com.ecommerce.promotion.model.response.PromotionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<DotGiamGia, String> {

    @Query(value = """
            SELECT
                dgg.id AS id,
                dgg.ma_dot_giam_gia AS ma,
                dgg.ten_dot_giam_gia AS ten,
                dgg.phan_tram AS phanTramGiam,
                dgg.mo_ta AS moTa,
                dgg.ngay_bat_dau AS ngayBatDau,
                dgg.ngay_ket_thuc AS ngayKetThuc,
                dgg.trang_thai_dot AS trangThai
            FROM dot_giam_gia dgg
            WHERE (:#{#req.ma} IS NULL OR :#{#req.ma} = '' OR dgg.ma_dot_giam_gia LIKE %:#{#req.ma}% OR dgg.ten_dot_giam_gia LIKE %:#{#req.ma}%)
              AND (:#{#req.phanTramGiam} IS NULL OR dgg.phan_tram = :#{#req.phanTramGiam})
              AND (:#{#req.trangThai} IS NULL OR dgg.trang_thai_dot = :#{#req.trangThai})
              AND ((:#{#req.ngayBatDau} IS NULL OR :#{#req.ngayKetThuc} IS NULL) OR (dgg.ngay_bat_dau >= :#{#req.ngayBatDau} AND dgg.ngay_ket_thuc <= :#{#req.ngayKetThuc}))
            GROUP BY dgg.id
            ORDER BY dgg.last_modified_date DESC
            """, nativeQuery = true)
    Page<PromotionResponse> getAllDotGiamGia(@Param("req") FindPromotionRequest req, Pageable pageable);

    @Query(value = """
            SELECT
                po.id AS id,
                po.ma_dot_giam_gia AS code,
                po.ten_dot_giam_gia AS name,
                po.phan_tram AS value,
                po.ngay_bat_dau AS startDate,
                po.ngay_ket_thuc AS endDate,
                po.trang_thai_dot AS status,
                (SELECT GROUP_CONCAT(DISTINCT ppd2.id_chi_tiet_san_pham) FROM dot_giam_gia_chi_tiet_san_pham ppd2 WHERE ppd2.trang_thai = 'DANG_SU_DUNG' AND ppd2.id_dot_giam_gia = po.id) AS productDetail,
                GROUP_CONCAT(DISTINCT ppd.id_chi_tiet_san_pham) AS productDetailUpdate,
                NULL AS product,
                GROUP_CONCAT(DISTINCT ppd.id) AS promotionProductDetail
            FROM dot_giam_gia po
                LEFT JOIN dot_giam_gia_chi_tiet_san_pham ppd ON po.id = ppd.id_dot_giam_gia
            WHERE po.id = :id
            GROUP BY po.id
            """, nativeQuery = true)
    PromotionByIdResponse getByIdPromotion(@Param("id") String id);

    Optional<DotGiamGia> findByTen(String name);

    @Query("SELECT d FROM DotGiamGia d JOIN DotGiamGiaChiTietSanPham dc ON d.id = dc.dotGiamGia.id WHERE dc.sanPhamChiTietId IN :productDetailIds")
    List<DotGiamGia> findAllByProductDetails(List<String> productDetailIds);
}
