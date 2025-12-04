package com.be.server.core.admin.banhang.repository;
import com.be.server.core.admin.banhang.model.response.ListHoaDon;
import com.be.server.entity.HoaDon;
import com.be.server.entity.PhieuGiamGia;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.repository.HoaDonRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ADTaoHoaDonRepository extends HoaDonRepository {

    @Query(value= """
    select hd.id as id,
            hd.ma as ma,
            sum(hdct.soLuong) as soLuong,
            hd.loaiHoaDon as loaiHoaDon
     from HoaDon hd 
    left join HoaDonChiTiet hdct on hd.id = hdct.hoaDon.id
    where hd.trangThaiHoaDon = 0 and (hd.loaiHoaDon = 0 or hd.loaiHoaDon = 1) 
    GROUP BY hd.id,hd.ma
    ORDER BY hd.id ASC
""")
    List<ListHoaDon> getAll();


    @Query("""
    SELECT DISTINCT pgg
    FROM PhieuGiamGia pgg
    LEFT JOIN PhieuGiamGiaChiTiet pggct ON pgg.id = pggct.phieuGiamGia.id
    WHERE pgg.status = 0
      AND pgg.dieuKien <= :tong
      AND pgg.soLuongPhieu > 0
      AND (
          pgg.loaiGiam = FALSE
          OR (pgg.loaiGiam = TRUE AND pggct.khachHang.id = :id)
      )
      AND NOT EXISTS (
          SELECT 1
          FROM HoaDon hd
          WHERE hd.voucher.id = pgg.id
            AND hd.khachHang.id = :id
      )
""")
    List<PhieuGiamGia> getPhieuGiamGia(@Param("id") String id, @Param("tong") Double tong);



    @Query(value= """
    SELECT pgg
    FROM PhieuGiamGia pgg
    LEFT JOIN PhieuGiamGiaChiTiet pggct ON pgg.id = pggct.phieuGiamGia.id
    WHERE pgg.status = 0
    and pgg.dieuKien >= :tong
    AND pgg.soLuongPhieu > 0
    AND ( 
        pgg.loaiGiam = FALSE
        OR (pgg.loaiGiam = TRUE AND (pggct.khachHang.id = :id))
    )
""")
    List<PhieuGiamGia> getPhieuGiamGiaKoDuDieuKien(@Param("id") String id, @Param("tong") Double tong);

    @Query(value= """
    SELECT pgg.id
    FROM HoaDonChiTiet pgg
    LEFT JOIN PhieuGiamGiaChiTiet pggct ON pgg.id = pggct.phieuGiamGia.id
    WHERE pgg.hoaDon.id = :id
""")
    List<String> getHDCT(@Param("id") String id);

    List<HoaDon> findByTrangThaiHoaDon(EntityTrangThaiHoaDon trangThaiHoaDon);
}
