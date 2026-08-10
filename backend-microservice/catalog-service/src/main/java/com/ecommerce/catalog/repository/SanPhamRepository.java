package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.SanPham;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.model.response.ListOptionResponse;
import com.ecommerce.catalog.model.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SanPhamRepository extends JpaRepository<SanPham, String> {
    List<SanPham> findByStatusOrderByCreatedDateDesc(EntityStatus status);

    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                sp.id AS id,
                sp.ma AS ma,
                sp.ten AS ten,
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
                sp.moTa AS moTa,
                SUM(spct.soLuong) AS tongSP,
                sp.status AS status
            FROM SanPham sp
                LEFT JOIN SanPhamChiTiet AS spct ON spct.sanPham.id = sp.id
                LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
                LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
                LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
                LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
                LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
            WHERE (:#{#rep.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#rep.q}, '%') OR sp.ma LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.danhMucId} IS NULL OR sp.danhMuc.id = :#{#rep.danhMucId})
                AND (:#{#rep.chatLieuId} IS NULL OR sp.chatLieu.id = :#{#rep.chatLieuId})
                AND (:#{#rep.thuongHieuId} IS NULL OR sp.thuongHieu.id = :#{#rep.thuongHieuId})
                AND (:#{#rep.loaiDeId} IS NULL OR sp.loaiDe.id = :#{#rep.loaiDeId})
                AND (:#{#rep.status} IS NULL OR sp.status = :#{#rep.entityStatus})
            GROUP BY sp.id, sp.ma, sp.ten, sp.moTa, sp.status, th.id, xx.id, ld.id, dm.id, cl.id
            ORDER BY sp.createdDate DESC
            """, countQuery = """
            SELECT COUNT(sp.id)
            FROM SanPham sp
                LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
                LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
                LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
                LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
                LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
            WHERE (:#{#rep.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#rep.q}, '%') OR sp.ma LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.danhMucId} IS NULL OR sp.danhMuc.id = :#{#rep.danhMucId})
                AND (:#{#rep.chatLieuId} IS NULL OR sp.chatLieu.id = :#{#rep.chatLieuId})
                AND (:#{#rep.thuongHieuId} IS NULL OR sp.thuongHieu.id = :#{#rep.thuongHieuId})
                AND (:#{#rep.loaiDeId} IS NULL OR sp.loaiDe.id = :#{#rep.loaiDeId})
                AND (:#{#rep.status} IS NULL OR sp.status = :#{#rep.entityStatus})
            """)
    Page<ProductResponse> getAllSanPhamByFilter(Pageable pageable, @Param("rep") ProductSearchRequest req);

    @Query("""
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                sp.id AS id,
                sp.ma AS ma,
                sp.ten AS ten,
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
                sp.moTa AS moTa,
                SUM(spct.soLuong) AS tongSP,
                sp.status AS status
            FROM SanPham sp
                LEFT JOIN SanPhamChiTiet AS spct ON sp.id = spct.sanPham.id
                LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
                LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
                LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
                LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
                LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
            WHERE sp.id LIKE CONCAT('%', :id, '%')
            GROUP BY sp.id, sp.ma, sp.ten, sp.moTa, sp.status, th.id, xx.id, ld.id, dm.id, cl.id
            """)
    Optional<ProductResponse> getAllSanPhamID(@Param("id") String id);

    @Query("select th.ten as ten, th.id as id from ThuongHieu th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListThuongHieu();

    @Query("select th.ten as ten, th.id as id from XuatSu th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListXuatXu();

    @Query("select th.ten as ten, th.id as id from LoaiDe th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getLoaiDe();

    @Query("select th.ten as ten, th.id as id from DanhMuc th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListDanhMuc();

    @Query("select th.ten as ten, th.id as id from KichCo th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListSize();

    @Query("select th.ten as ten, th.id as id from MauSac th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListMau();

    @Query("select th.ten as ten, th.id as id from ChatLieu th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListChatLieu();
}
