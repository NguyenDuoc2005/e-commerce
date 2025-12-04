package com.be.server.core.permitall.sanpham.repository;

import com.be.server.core.permitall.sanpham.dto.response.PMMauSacDTO;
import com.be.server.repository.MauSacRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PMMauSacRepository extends MauSacRepository {
    @Query(value = """
    SELECT DISTINCT new com.be.server.core.permitall.sanpham.dto.response.PMMauSacDTO(ms.ten, ms.mau)
    FROM SanPhamChiTiet spct
    JOIN spct.mauSac ms
    WHERE spct.sanPham.id = :sanPhamId
     and spct.status = 0 
    """
    )
    List<PMMauSacDTO> getBySanPhamId(@Param("sanPhamId") String sanPhamId);


}
