package com.be.server.core.admin.sanpham.repository;

import com.be.server.core.admin.sanpham.model.request.ADSanPhamSearchRequest;
import com.be.server.core.admin.sanpham.model.response.ADListThuongHieu;
import com.be.server.core.admin.sanpham.model.response.ADSanPhamResponse;
import com.be.server.repository.SanPhamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ADSanPhamRepository extends SanPhamRepository {

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
    FROM
        SanPham sp
        LEFT JOIN SanPhamChiTiet AS spct ON  spct.sanPham.id = sp.id
        LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
        LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
        LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
        LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
        LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
    WHERE
        (:#{#rep.q} IS NULL OR
        sp.ten LIKE CONCAT('%', :#{#rep.q}, '%') OR
        sp.ma LIKE CONCAT('%', :#{#rep.q}, '%'))
        AND (:#{#rep.danhMucId} IS NULL OR sp.danhMuc.id = :#{#rep.danhMucId})
        AND (:#{#rep.chatLieuId} IS NULL OR sp.chatLieu.id = :#{#rep.chatLieuId})
        AND (:#{#rep.thuongHieuId} IS NULL OR sp.thuongHieu.id = :#{#rep.thuongHieuId})
        AND (:#{#rep.loaiDeId} IS NULL OR sp.loaiDe.id = :#{#rep.loaiDeId})
        AND (:#{#rep.status} IS NULL OR sp.status = :#{#rep.entityStatus})
    GROUP BY sp.id, sp.ma, sp.ten, sp.moTa, sp.status, th.id, xx.id, ld.id, dm.id, cl.id 
    ORDER BY sp.createdDate DESC
    """, countQuery = """
    SELECT
        COUNT(sp.id)
    FROM
        SanPham sp
        LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
        LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
        LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
        LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
        LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id
    WHERE
        (:#{#rep.q} IS NULL OR
        sp.ten LIKE CONCAT('%', :#{#rep.q}, '%') OR
        sp.ma LIKE CONCAT('%', :#{#rep.q}, '%'))
        AND (:#{#rep.danhMucId} IS NULL OR sp.danhMuc.id = :#{#rep.danhMucId})
        AND (:#{#rep.chatLieuId} IS NULL OR sp.chatLieu.id = :#{#rep.chatLieuId})
        AND (:#{#rep.thuongHieuId} IS NULL OR sp.thuongHieu.id = :#{#rep.thuongHieuId})
        AND (:#{#rep.loaiDeId} IS NULL OR sp.loaiDe.id = :#{#rep.loaiDeId})
        AND (:#{#rep.status} IS NULL OR sp.status = :#{#rep.entityStatus})
    """
    )
    Page<ADSanPhamResponse> getAllSanPhamByFilter(Pageable pageable, @Param("rep") ADSanPhamSearchRequest req);

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
        FROM 
            SanPham sp
            LEFT JOIN SanPhamChiTiet AS spct ON  sp.id = spct.sanPham.id 
              LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
            LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
            LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
            LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
                        LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id  
        WHERE 
            sp.id LIKE CONCAT('%', :#{#rep}, '%')           
        GROUP BY sp.id,sp.ma,sp.ten,sp.moTa,sp.status,th.id
        """ )
    Optional<ADSanPhamResponse> getAllSanPhamID(@Param("rep") String id);

    @Query(value=
        """
                select 
                       th.ten as ten,
                       th.id as id
                      from ThuongHieu th 
                      where th.status = 0
                                      order by th.createdDate DESC      
                """
    )
    List<ADListThuongHieu> getListThuongHieu();

    @Query(value=
            """
                    select 
                           th.ten as ten,
                           th.id as id
                          from XuatSu th 
                          where th.status = 0
                                              order by th.createdDate DESC      
                    """
    )
    List<ADListThuongHieu> getListXuatXu();

    @Query(value=
            """
                    select 
                           th.ten as ten,
                           th.id as id
                          from LoaiDe th 
                          where th.status = 0
                                              order by th.createdDate DESC      
                    """
    )
    List<ADListThuongHieu> getLoaiDe();

    @Query(value=
            """
                    select 
                           th.ten as ten,
                           th.id as id
                          from DanhMuc th 
                          where th.status = 0
                          order by th.createdDate DESC                  
                    """
    )
    List<ADListThuongHieu> getListDanhMuc();

    @Query(value=
            """
                    select 
                           th.ten as ten,
                           th.id as id
                          from KichCo th 
                          where th.status = 0
                    """
    )
    List<ADListThuongHieu> getListSize();

    @Query(value=
            """
                    select 
                           th.ten as ten,
                           th.id as id
                          from MauSac th 
                          where th.status = 0
                                              order by th.createdDate DESC      
                    """
    )
    List<ADListThuongHieu> getListMau();

    @Query(value=
            """
                    select 
                           th.ten as ten,
                           th.id as id
                          from ChatLieu th 
                          where th.status = 0
                                              order by th.createdDate DESC      
                    """
    )
    List<ADListThuongHieu> getListChatLieu();
}
