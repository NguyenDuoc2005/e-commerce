package com.be.server.core.permitall.sanpham.repository;

import com.be.server.core.permitall.sanpham.dto.response.PMKichCoDTO;
import com.be.server.repository.SizeRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PMKichCoRepository extends SizeRepository {
    @Query(value = """
    SELECT DISTINCT new com.be.server.core.permitall.sanpham.dto.response.PMKichCoDTO(kc.ten)
    FROM SanPhamChiTiet spct
    JOIN spct.kichCo kc
    WHERE spct.sanPham.id = :sanPhamId
        AND spct.status = 0 
    """
    )
    List<PMKichCoDTO> getDistinctBySanPhamId(@Param("sanPhamId") String sanPhamId);



}
