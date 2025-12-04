package com.be.server.core.permitall.sanpham.repository;

import com.be.server.core.permitall.sanpham.dto.request.PMDanhSachSanPhamSearchRequest;
import com.be.server.core.permitall.sanpham.dto.response.PMSanPhamMoiResponse;
import com.be.server.repository.SanPhamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PMSanPhamRepository extends SanPhamRepository {

    @Query(value = """
    SELECT new com.be.server.core.permitall.sanpham.dto.response.PMSanPhamMoiResponse(
        sp.id,
        sp.ten,
        MIN(sct.anh),
        th.ten,
        dm.ten,
        cl.ten,
        xs.ten,
        sp.moTa,
        CAST(MIN(sct.giaBan) AS double),
        sp.createdDate
    )
    FROM SanPham sp, SanPhamChiTiet sct
    LEFT JOIN sp.thuongHieu th
    LEFT JOIN sp.danhMuc dm
    LEFT JOIN sp.chatLieu cl
    LEFT JOIN sp.xuatSu xs
    WHERE sct.sanPham.id = sp.id
        AND sct.status = 0 AND sp.status = 0 
    GROUP BY sp.id, sp.ten, th.ten, dm.ten, cl.ten, xs.ten, sp.moTa, sp.createdDate
     order by sp.createdDate desc 
    """,
            countQuery = "SELECT COUNT(sp) FROM SanPham sp")
    Page<PMSanPhamMoiResponse> getSanPhamMoiResponsePage(Pageable pageable);


    @Query(value = """
    SELECT new com.be.server.core.permitall.sanpham.dto.response.PMSanPhamMoiResponse(
        sp.id,
        sp.ten,
        MIN(sct.anh),
        th.ten,
        dm.ten,
        cl.ten,
        xs.ten,
        sp.moTa,
        CAST(MIN(sct.giaBan) AS double),
        sp.createdDate
    )
    FROM SanPham sp
    JOIN SanPhamChiTiet sct ON sct.sanPham.id = sp.id
    LEFT JOIN sp.thuongHieu th
    LEFT JOIN sp.danhMuc dm
    LEFT JOIN sp.chatLieu cl
    LEFT JOIN sp.xuatSu xs
    LEFT JOIN sp.loaiDe ld
    WHERE sct.status = 0 AND sp.status = 0
        AND (:#{#request.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#request.q}, '%'))
        AND (:#{#request.giaMin} IS NULL OR sct.giaBan >= :#{#request.giaMin})
        AND (:#{#request.giaMax} IS NULL OR sct.giaBan <= :#{#request.giaMax})
        AND (:thuongHieuIds IS NULL OR th.id IN :thuongHieuIds)
         AND (:danhMucIds IS NULL OR dm.id IN :danhMucIds)
         AND (:chatLieuIds IS NULL OR cl.id IN :chatLieuIds)
         AND (:loaiDeIds IS NULL OR ld.id IN :loaiDeIds)
    GROUP BY sp.id, sp.ten, th.ten, dm.ten, cl.ten, xs.ten, sp.moTa, sp.createdDate
    ORDER BY
        CASE :sortBy
            WHEN 'giaBan_asc' THEN MIN(sct.giaBan)
            WHEN 'createdAt_asc' THEN sp.createdDate
            WHEN 'ten_asc' THEN sp.ten
        END ASC,
        CASE :sortBy
            WHEN 'giaBan_desc' THEN MIN(sct.giaBan)
            WHEN 'createdAt_desc' THEN sp.createdDate
            WHEN 'ten_desc' THEN sp.ten
        END DESC
""",
            countQuery = """
    SELECT COUNT(DISTINCT sp)
    FROM SanPham sp
    JOIN SanPhamChiTiet sct ON sct.sanPham.id = sp.id
    LEFT JOIN sp.thuongHieu th
    LEFT JOIN sp.danhMuc dm
    LEFT JOIN sp.chatLieu cl
    LEFT JOIN sp.xuatSu xs
    LEFT JOIN sp.loaiDe ld
    WHERE sct.status = 0 AND sp.status = 0
        AND (:#{#request.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#request.q}, '%'))
        AND (:#{#request.giaMin} IS NULL OR sct.giaBan >= :#{#request.giaMin})
        AND (:#{#request.giaMax} IS NULL OR sct.giaBan <= :#{#request.giaMax})
          AND (:thuongHieuIds IS NULL OR th.id IN :thuongHieuIds)
           AND (:danhMucIds IS NULL OR dm.id IN :danhMucIds)
           AND (:chatLieuIds IS NULL OR cl.id IN :chatLieuIds)
           AND (:loaiDeIds IS NULL OR ld.id IN :loaiDeIds)
""")
    Page<PMSanPhamMoiResponse> getDanhSachSanPhamResponsePage(
            Pageable pageable,
            @Param("request") PMDanhSachSanPhamSearchRequest request,
            @Param("sortBy") String sortBy,
            @Param("thuongHieuIds") List<String> thuongHieuIds,
            @Param("chatLieuIds") List<String> chatLieuIds,
            @Param("loaiDeIds") List<String> loaiDeIds,
            @Param("danhMucIds") List<String> danhMucIds
    );

    @Query(value = """
    SELECT new com.be.server.core.permitall.sanpham.dto.response.PMSanPhamMoiResponse(
        sp.id,
        sp.ten,
        MIN(sct.anh),
        th.ten,
        dm.ten,
        cl.ten,
        xs.ten,
        sp.moTa,
        CAST(MIN(sct.giaBan) AS double),
        sp.createdDate
    )
    FROM SanPham sp
    JOIN SanPhamChiTiet sct ON sct.sanPham.id = sp.id
    LEFT JOIN sp.thuongHieu th
    LEFT JOIN sp.danhMuc dm
    LEFT JOIN sp.chatLieu cl
    LEFT JOIN sp.xuatSu xs
    JOIN DotGiamGiaChiTietSanPham dggctsp ON dggctsp.sanPhamChiTiet.id = sct.id
    JOIN DotGiamGia dgg ON dggctsp.dotGiamGia.id = dgg.id
    WHERE
        sp.status = 0
        AND sct.status = 0
        AND dggctsp.trangThai = 'DANG_SU_DUNG'
        AND dgg.trangThai = 'DANG_KICH_HOAT'
        AND dgg.ngayBatDau <= :now
        AND dgg.ngayKetThuc >= :now
    GROUP BY sp.id, sp.ten, th.ten, dm.ten, cl.ten, xs.ten, sp.moTa, sp.createdDate
    ORDER BY sp.createdDate DESC
    """
    )
    Page<PMSanPhamMoiResponse> getSanPhamGiamGiaResponsePage(@Param("now") Long now, Pageable pageable);





}
