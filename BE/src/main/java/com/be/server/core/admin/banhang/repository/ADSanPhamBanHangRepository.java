package com.be.server.core.admin.banhang.repository;

import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.SanPhamChiTiet.model.response.ADSanPhamChiTietResponse;
import com.be.server.core.admin.banhang.model.request.ListSanPhamRequest;
import com.be.server.repository.SanPhamChiTietRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ADSanPhamBanHangRepository extends SanPhamChiTietRepository {

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
    FROM 
        SanPhamChiTiet spct
        LEFT JOIN SanPham AS sp ON spct.sanPham.id = sp.id
        LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
        LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
        LEFT JOIN KichCo AS kc ON kc.id = spct.kichCo.id
        LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
        LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
        LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id  
        LEFT JOIN MauSac AS ms ON ms.id = spct.mauSac.id
    WHERE 
        spct.soLuong > 0 and spct.status = 0 And
         (( :#{#req.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#req.q}, '%') 
            OR spct.ma LIKE CONCAT('%', :#{#req.q}, '%') )
        AND ( :#{#req.status} IS NULL OR spct.status = :#{#req.status} )
        AND ( :#{#req.idMauSac} IS NULL OR spct.mauSac.id = :#{#req.idMauSac} )
        AND ( :#{#req.idKichThuoc} IS NULL OR spct.kichCo.id = :#{#req.idKichThuoc} )
        AND ( :#{#req.idDanhMuc} IS NULL OR sp.danhMuc.id = :#{#req.idDanhMuc} )
        AND ( :#{#req.idChatLieu} IS NULL OR sp.chatLieu.id = :#{#req.idChatLieu} )
        AND ( :#{#req.idThuongHieu} IS NULL OR sp.thuongHieu.id = :#{#req.idThuongHieu} )
        AND ( :#{#req.idLoaiDe} IS NULL OR sp.loaiDe.id = :#{#req.idLoaiDe} ))
    ORDER BY spct.createdDate DESC
    """, countQuery = """
    SELECT 
        COUNT(spct.id)
    FROM 
        SanPhamChiTiet spct
        LEFT JOIN SanPham AS sp ON spct.sanPham.id = sp.id
        LEFT JOIN ThuongHieu AS th ON th.id = sp.thuongHieu.id
        LEFT JOIN XuatSu AS xx ON xx.id = sp.xuatSu.id
        LEFT JOIN KichCo AS kc ON kc.id = spct.kichCo.id
        LEFT JOIN LoaiDe AS ld ON ld.id = sp.loaiDe.id
        LEFT JOIN DanhMuc AS dm ON dm.id = sp.danhMuc.id
        LEFT JOIN ChatLieu AS cl ON cl.id = sp.chatLieu.id  
        LEFT JOIN MauSac AS ms ON ms.id = spct.mauSac.id
    WHERE 
         spct.soLuong > 0 and spct.status = 0 And
       ( ( :#{#req.q} IS NULL OR sp.ten LIKE CONCAT('%', :#{#req.q}, '%') 
            OR spct.ma LIKE CONCAT('%', :#{#req.q}, '%') )
        AND ( :#{#req.status} IS NULL OR spct.status = :#{#req.status} )
        AND ( :#{#req.idMauSac} IS NULL OR spct.mauSac.id = :#{#req.idMauSac} )
        AND ( :#{#req.idKichThuoc} IS NULL OR spct.kichCo.id = :#{#req.idKichThuoc} )
        AND ( :#{#req.idDanhMuc} IS NULL OR sp.danhMuc.id = :#{#req.idDanhMuc} )
        AND ( :#{#req.idChatLieu} IS NULL OR sp.chatLieu.id = :#{#req.idChatLieu} )
        AND ( :#{#req.idThuongHieu} IS NULL OR sp.thuongHieu.id = :#{#req.idThuongHieu} )
        AND ( :#{#req.idLoaiDe} IS NULL OR sp.loaiDe.id = :#{#req.idLoaiDe} ))
    """)
    Page<ADSanPhamChiTietResponse> getAllSanPhamChiTietByFilter(Pageable pageable, @Param("req") ListSanPhamRequest req);
}