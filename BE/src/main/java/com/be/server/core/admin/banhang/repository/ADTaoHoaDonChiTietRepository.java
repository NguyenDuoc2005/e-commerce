package com.be.server.core.admin.banhang.repository;

import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.SanPhamChiTiet.model.response.ADSanPhamChiTietResponse;
import com.be.server.core.admin.SanPhamChiTiet.model.response.ADThemSanPhamChiTietResponse;
import com.be.server.core.admin.banhang.model.request.ADThemGioHangRequest;
import com.be.server.core.admin.banhang.model.request.ADThemSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ADXoaSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ListKhachHangRequest;
import com.be.server.core.admin.banhang.model.response.ADChonKhachHangRespones;
import com.be.server.core.admin.banhang.model.response.ADGioHangRespones;
import com.be.server.core.admin.banhang.model.response.ADPhuongThucThanhToanRespones;
import com.be.server.core.admin.banhang.model.response.ADThanhToanRespones;
import com.be.server.core.permitall.donmua.model.request.PMThemSanPhamRequest;
import com.be.server.entity.HoaDon;
import com.be.server.entity.KhachHang;
import com.be.server.repository.HoaDonChiTietRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ADTaoHoaDonChiTietRepository extends HoaDonChiTietRepository {

    @Query(value = """
   SELECT 
        ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
        hdct.id as idHDCT,
        spct.id AS id,    
        sp.ten AS ten,
        hdct.soLuong as soLuong,
        hdct.gia as giaBan,
        s.ten as kichThuoc,
        ms.mau as mau,  
        spct.anh as anh
    FROM 
        HoaDonChiTiet hdct
        LEFT JOIN SanPhamChiTiet as spct ON hdct.spct.id = spct.id
        LEFT JOIN SanPham AS sp ON spct.sanPham.id = sp.id
        LEFT JOIN MauSac AS ms ON ms.id = spct.mauSac.id
        LEFT JOIN KichCo AS s ON s.id = spct.kichCo.id
        LEFT JOIN HoaDon  as hd ON hd.id = hdct.hoaDon.id
    WHERE 
        hd.id = :#{#rep}
    ORDER BY spct.createdDate DESC
    """)
    List<ADGioHangRespones> getAllGioHang(@Param("rep") String req);

    @Query(value = """
    select hdct.id from HoaDonChiTiet hdct
    where hdct.id = :#{#rep.idHDCT}
""")
    String getHoaDonChiTiet(@Param("rep") ADXoaSanPhamRequest req);

    @Query(value = """
    select hdct.id from HoaDonChiTiet hdct
    left join SanPhamChiTiet spct on spct.id = hdct.spct.id
    where hdct.hoaDon.id = :#{#rep.idHD} and hdct.spct.id = :#{#rep.idSP}
    order by hdct.createdDate DESC
""")
    List<String> checkGioHang(@Param("rep") ADThemSanPhamRequest req);



    @Query(value = """
    select hdct.id from HoaDonChiTiet hdct
    left join SanPhamChiTiet spct on spct.id = hdct.spct.id
    where hdct.hoaDon.id = :#{#rep.idHD} and hdct.spct.id = :#{#rep.idSP}
    order by hdct.createdDate DESC
""")
    List<String> checkGioHangV2(@Param("rep") PMThemSanPhamRequest req);


    @Query(value = """
    select hdct.id from HoaDonChiTiet hdct
    left join SanPhamChiTiet spct on spct.id = hdct.spct.id
    where hdct.id = :#{#rep.idHDCT} 
    order by hdct.createdDate DESC
""")
    String checkGioHangHuy(@Param("rep") ADThemSanPhamRequest req);

    @Query(value = """
        SELECT kh.id AS id, kh.ten AS ten, kh.sdt AS sdt 
        FROM KhachHang kh 
        WHERE kh.status = 0 
         AND ((:#{#req.q} IS NULL OR :#{#req.q} = '' OR kh.ten LIKE CONCAT('%', :#{#req.q}, '%') OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%'))
         OR (:#{#req.q} IS NULL OR :#{#req.q} = '' OR kh.ma LIKE CONCAT('%', :#{#req.q}, '%') OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%'))
         OR (:#{#req.q} IS NULL OR :#{#req.q} = '' OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%') OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%'))
        )   
        """,
            countQuery = """
        SELECT COUNT(*) 
        FROM KhachHang kh 
        WHERE kh.status = 0 
        AND ((:#{#req.q} IS NULL OR :#{#req.q} = '' OR kh.ten LIKE CONCAT('%', :#{#req.q}, '%') OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%'))
         OR (:#{#req.q} IS NULL OR :#{#req.q} = '' OR kh.ma LIKE CONCAT('%', :#{#req.q}, '%') OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%'))
         OR (:#{#req.q} IS NULL OR :#{#req.q} = '' OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%') OR kh.sdt LIKE CONCAT('%', :#{#req.q}, '%'))
                )
    """)
    Page<ADChonKhachHangRespones> getAllList(@Param("req") ListKhachHangRequest request, Pageable pageable);

    @Query(value= """
    select kh.id as id , kh.ten as ten, kh.sdt as sdt, kh.diaChi as diaChi, kh.tinh as tinh, kh.huyen as huyen, kh.xa as xa  from HoaDon hd 
    left Join KhachHang kh on kh.id = hd.khachHang.id
    where hd.id = :#{#rep}
""")
    ADChonKhachHangRespones getKhachHang(@Param("rep") String req);

    @Query(value= """
    select SUM(hdct.gia * hdct.soLuong) as tongTien  from HoaDon hd 
    left Join HoaDonChiTiet hdct on hd.id = hdct.hoaDon.id
    where hd.id = :#{#rep}
""")
    ADThanhToanRespones getThanhToan(@Param("rep") String req);

    @Query(value= """
    select SUM(hdct.gia * hdct.soLuong) as tongTien, hd.phuongThucThanhToan as phuongThucThanhToan  from HoaDon hd 
    left Join HoaDonChiTiet hdct on hd.id = hdct.hoaDon.id
    where hd.id = :#{#rep}
""")
    List<ADPhuongThucThanhToanRespones> getPhuongThucThanhToan(@Param("rep") String req);


    @Query(value = """
    SELECT hdct.id 
    FROM HoaDon hd 
    LEFT JOIN HoaDonChiTiet hdct ON hd.id = hdct.hoaDon.id 
    WHERE hd.id = :id
    """)
   List<String> getHoaDonChiTiet(@Param("id") String id);

    @Query(value = """
    SELECT lstt.id 
    FROM HoaDon hd 
    LEFT JOIN LichSuTrangThaiHoaDon lstt ON hd.id = lstt.hoaDon.id 
    WHERE hd.id = :id
    """)
    String getHoaDonLichSuTrangThai(@Param("id") String id);


    @Query(value = """
    SELECT lstt.id 
    FROM HoaDonChiTiet hd 
    LEFT JOIN SanPhamChiTiet lstt ON lstt.id = hd.spct.id 
    WHERE hd.id = :id
    """)
    String getSanPhamChiTiet(@Param("id") String id);

    @Query(value = """
    SELECT lstt.id 
    FROM HoaDonChiTiet hd 
    LEFT JOIN SanPhamChiTiet lstt ON lstt.id = hd.spct.id 
    WHERE hd.id = :id
    """)
    List<String> getListSanPhamChiTiet(@Param("id") String id);
}
