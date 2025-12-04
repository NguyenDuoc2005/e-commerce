package com.be.server.core.permitall.thanhtoan.repository;

import com.be.server.core.permitall.thanhtoan.model.Respones.ListSP;
import com.be.server.core.permitall.thanhtoan.model.Respones.ListSPRespone;
import com.be.server.repository.HoaDonChiTietRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PHHoaDonChiTietRepository extends HoaDonChiTietRepository {

    @Query("""
    SELECT spct.id as id, 
           hdct.soLuong as quantity
    FROM HoaDonChiTiet hdct
    JOIN SanPhamChiTiet spct ON spct.id = hdct.spct.id
    JOIN HoaDon hd ON hd.id = hdct.hoaDon.id
    WHERE hd.id = :idGH
""")
    List<ListSPRespone> getIDSP(@Param("idGH") String idGH);


}
