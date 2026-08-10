package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.SanPhamChiTiet;
import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.catalog.model.response.ListOptionResponse;
import com.ecommerce.catalog.model.response.ProductDetailEditResponse;
import com.ecommerce.catalog.model.response.ProductDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, String> {
    List<SanPhamChiTiet> findBySanPhamIdAndStatusOrderByCreatedDateDesc(String productId, EntityStatus status);

    List<SanPhamChiTiet> findByIdIn(List<String> ids);

    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                spct.id AS id,
                sp.ten AS ten,
                spct.soLuong AS soLuong,
                th.ten AS tenThuongHieu,
                ld.ten AS tenLoaiDe,
                cl.ten AS tenChatLieu,
                dm.ten AS tenDanhMuc,
                spct.giaBan AS giaBan,
                kc.ten AS kichThuoc,
                ms.mau AS mau,
                ms.ten AS tenMau,
                spct.anh AS anh,
                spct.status AS status,
                (SELECT MAX(spct2.giaBan) FROM SanPhamChiTiet spct2) AS giaMax
            FROM SanPhamChiTiet spct
                LEFT JOIN SanPham AS sp ON spct.sanPham.id = sp.id
                LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
                LEFT JOIN KichCo AS kc ON kc.id = spct.kichCo.id
                LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
                LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
                LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
                LEFT JOIN MauSac AS ms ON ms.id = spct.mauSac.id
            WHERE (:#{#rep.idSP} IS NULL OR spct.sanPham.id = :#{#rep.idSP})
                AND (:#{#rep.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#rep.q}, '%') OR spct.ma LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.entityStatus} IS NULL OR spct.status = :#{#rep.entityStatus})
                AND (:#{#rep.priceMin} IS NULL OR spct.giaBan >= :#{#rep.priceMin})
                AND (:#{#rep.priceMax} IS NULL OR spct.giaBan <= :#{#rep.priceMax})
                AND (:#{#rep.idKT} IS NULL OR kc.id = :#{#rep.idKT})
                AND (:#{#rep.idMS} IS NULL OR ms.id = :#{#rep.idMS})
            ORDER BY spct.createdDate DESC
            """, countQuery = """
            SELECT COUNT(spct.id)
            FROM SanPhamChiTiet spct
                LEFT JOIN SanPham AS sp ON spct.sanPham.id = sp.id
                LEFT JOIN KichCo AS kc ON kc.id = spct.kichCo.id
                LEFT JOIN MauSac AS ms ON ms.id = spct.mauSac.id
            WHERE (:#{#rep.idSP} IS NULL OR spct.sanPham.id = :#{#rep.idSP})
                AND (:#{#rep.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#rep.q}, '%') OR spct.ma LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.entityStatus} IS NULL OR spct.status = :#{#rep.entityStatus})
                AND (:#{#rep.priceMin} IS NULL OR spct.giaBan >= :#{#rep.priceMin})
                AND (:#{#rep.priceMax} IS NULL OR spct.giaBan <= :#{#rep.priceMax})
                AND (:#{#rep.idKT} IS NULL OR kc.id = :#{#rep.idKT})
                AND (:#{#rep.idMS} IS NULL OR ms.id = :#{#rep.idMS})
            """)
    Page<ProductDetailResponse> getAllSanPhamChiTietByFilter(Pageable pageable, @Param("rep") ProductDetailSearchRequest req);

    @Query("""
            SELECT
                ROW_NUMBER() OVER (ORDER BY spct.id DESC) AS stt,
                spct.id AS id,
                sp.ten AS ten,
                sp.moTa AS moTa,
                th.ten AS tenThuongHieu,
                th.id AS idThuongHieu,
                xx.ten AS tenXuatXu,
                xx.id AS idXuatXu,
                ld.ten AS tenLoaiDe,
                ld.id AS idLoaiDe,
                dm.ten AS tenDanhMuc,
                dm.id AS idDanhMuc,
                cl.ten AS tenChatLieu,
                cl.id AS idChatLieu,
                ms.ten AS tenMauSac,
                ms.id AS idMauSac,
                kc.ten AS tenKichThuoc,
                kc.id AS idKichThuoc,
                spct.soLuong AS soLuong,
                spct.giaBan AS giaBan,
                spct.anh AS anh
            FROM SanPhamChiTiet spct
                LEFT JOIN SanPham AS sp ON sp.id = spct.sanPham.id
                LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
                LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
                LEFT JOIN MauSac AS ms ON ms.id = spct.mauSac.id
                LEFT JOIN KichCo AS kc ON kc.id = spct.kichCo.id
                LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
                LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
                LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
            WHERE spct.id LIKE CONCAT('%', :id, '%')
            """)
    java.util.Optional<ProductDetailEditResponse> getSanPhamChiTietID(@Param("id") String id);

    @Query("""
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                sp.id AS id,
                sp.ten AS ten,
                sp.moTa AS moTa,
                th.ten AS tenThuongHieu,
                th.id AS idThuongHieu,
                xx.ten AS tenXuatXu,
                xx.id AS idXuatXu,
                ld.ten AS tenLoaiDe,
                ld.id AS idLoaiDe,
                dm.ten AS tenDanhMuc,
                dm.id AS idDanhMuc,
                cl.ten AS tenChatLieu,
                cl.id AS idChatLieu
            FROM SanPham sp
                LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
                LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
                LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
                LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
                LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
            WHERE sp.id LIKE CONCAT('%', :id, '%')
            """)
    java.util.Optional<ProductDetailEditResponse> getSanPhamID(@Param("id") String id);

    @Query("select th.ten as ten, th.id as id from KichCo th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListSize();

    @Query("select th.ten as ten, th.id as id from MauSac th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListColor();

    @Query("""
            select distinct spct.id
            from SanPhamChiTiet spct
            where spct.mauSac.id = :idMau
              and spct.kichCo.id = :idKichCo
              and spct.sanPham.id = :idSanPham
            """)
    String checkThemSanPham(@Param("idMau") String idMau, @Param("idKichCo") String idKichCo, @Param("idSanPham") String idSanPham);

    @Query("select distinct sp.id from SanPham sp where sp.ten = :tenSP")
    String checkThemSP(@Param("tenSP") String tenSP);

    @Query("""
            select spct.id
            from SanPhamChiTiet spct
            left join SanPham sp on spct.sanPham.id = sp.id
            where sp.id = :id
            """)
    List<String> checkIdSanPhamCT(@Param("id") String id);

    @Query("select sp.id as id, sp.ten as ten from SanPham sp where sp.status = 0")
    List<ListOptionResponse> getListThemSP();
}
