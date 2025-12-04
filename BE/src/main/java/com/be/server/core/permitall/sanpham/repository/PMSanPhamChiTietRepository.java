package com.be.server.core.permitall.sanpham.repository;

import com.be.server.repository.SanPhamChiTietRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PMSanPhamChiTietRepository extends SanPhamChiTietRepository {

    @Query("SELECT sct.anh FROM SanPhamChiTiet sct WHERE sct.sanPham.id = :sanPhamId and sct.status = 0")
    List<String> getAllAnhBySanPhamId(@Param("sanPhamId") String sanPhamId);


}
