package com.be.server.core.admin.dotgiamgia.repository;

import com.be.server.core.admin.dotgiamgia.model.request.FindDotGiamGiaRequest;
import com.be.server.core.admin.dotgiamgia.model.response.DotGiamGiaByIdResponse;
import com.be.server.core.admin.dotgiamgia.model.response.DotGiamGiaResponse;
import com.be.server.core.admin.dotgiamgia.model.response.PromotionByProDuctDetail;
import com.be.server.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ADDotGiamGiaRepository extends com.be.server.repository.DotGiamGiaRepository {

    @Query(value = """
            SELECT 
                dgg.id AS id,
                dgg.ma_dot_giam_gia AS ma,
                dgg.ten_dot_giam_gia AS ten,
                dgg.phan_tram AS phanTramGiam,
                dgg.mo_ta AS moTa,
                dgg.ngay_bat_dau AS ngayBatDau,
                dgg.ngay_ket_thuc AS ngayKetThuc,
                dgg.trang_thai AS trangThai
            FROM dot_giam_gia dgg
            WHERE
                (:#{#req.ma} IS NULL OR :#{#req.ma} = '' OR dgg.ma_dot_giam_gia LIKE %:#{#req.ma}% OR :#{#req.ma} = '' OR dgg.ten_dot_giam_gia LIKE %:#{#req.ma}%)
                AND (:#{#req.phanTramGiam} IS NULL OR dgg.phan_tram = :#{#req.phanTramGiam})
                AND (:#{#req.trangThai} IS NULL OR dgg.trang_thai = :#{#req.trangThai})
                AND (
                     (:#{#req.ngayBatDau} IS NULL OR :#{#req.ngayKetThuc} IS NULL)
                     OR (
                         dgg.ngay_bat_dau >= :#{#req.ngayBatDau}
                         AND dgg.ngay_ket_thuc <= :#{#req.ngayKetThuc}
                     )
                 )
            GROUP BY dgg.id
            ORDER BY dgg.last_modified_date DESC
            """, nativeQuery = true)
    Page<DotGiamGiaResponse> getAllDotGiamGia(@Param("req") FindDotGiamGiaRequest req, Pageable pageable);

    @Query(value = """
            SELECT
                po.id as id,
                po.ma_dot_giam_gia as code,
                po.ten_dot_giam_gia as name,
                po.phan_tram as value,
                po.ngay_bat_dau as startDate,
                po.ngay_ket_thuc as endDate,
                po.trang_thai as status,
                (select GROUP_CONCAT(DISTINCT pd.id ) from san_pham_chi_tiet pd join dot_giam_gia_chi_tiet_san_pham ppd on pd.id = ppd.id_chi_tiet_san_pham
                 where ppd.trang_thai ='DANG_SU_DUNG' and ppd.id_dot_giam_gia = po.id) as productDetail,
                GROUP_CONCAT(DISTINCT pd.id ) as productDetailUpdate,
                (select GROUP_CONCAT(DISTINCT p1.id ) from san_pham_chi_tiet pd
                                                               join dot_giam_gia_chi_tiet_san_pham ppd on pd.id = ppd.id_chi_tiet_san_pham
                                                               JOIN san_pham p1 on p1.id = pd.id_san_pham

                 where  ppd.id_dot_giam_gia = po.id and p1.id in (select pd.id_san_pham from san_pham_chi_tiet pd join dot_giam_gia_chi_tiet_san_pham ppd on pd.id = ppd.id_chi_tiet_san_pham
                                                               where ppd.trang_thai ='DANG_SU_DUNG'and ppd.id_dot_giam_gia = po.id )) as product,
                GROUP_CONCAT(DISTINCT ppd.id) AS promotionProductDetail

            FROM dot_giam_gia po
                     LEFT JOIN dot_giam_gia_chi_tiet_san_pham ppd on po.id = ppd.id_dot_giam_gia
                     LEFT JOIN san_pham_chi_tiet pd on pd.id = ppd.id_chi_tiet_san_pham
                     LEFT JOIN san_pham p on p.id = pd.id_san_pham
                                         where po.id = :id
                                         group by po.id
                                         """, nativeQuery = true)
    DotGiamGiaByIdResponse getByIdPromotion(@Param("id") String id);

    @Query(value = """
                 select
                  pd.anh_san_pham AS image,
                  p.ma_san_pham AS code,
                  p.ten_san_pham AS name,
                  pr.ten_dot_giam_gia AS namePromotion,
                  pr.phan_tram AS valuePromotion,
                  ppd.trang_thai AS statusPromotion
                  from san_pham_chi_tiet pd 
                  JOIN san_pham p on p.id = pd.id_san_pham
                 JOIN dot_giam_gia_chi_tiet_san_pham ppd on ppd.id_chi_tiet_san_pham = pd.id
                 JOIN dot_giam_gia pr on pr.id = ppd.id_dot_giam_gia
                 
                 where pd.id =:id
            """, nativeQuery = true)
    List<PromotionByProDuctDetail> getByIdProductDetail(@Param("id") String id);

    Optional<DotGiamGia> findByTen(String name);

    @Query("SELECT d FROM DotGiamGia d JOIN DotGiamGiaChiTietSanPham dc ON d.id = dc.dotGiamGia.id WHERE dc.sanPhamChiTiet.id IN :productDetailIds")
    List<DotGiamGia> findAllByProductDetails(List<String> productDetailIds);

}