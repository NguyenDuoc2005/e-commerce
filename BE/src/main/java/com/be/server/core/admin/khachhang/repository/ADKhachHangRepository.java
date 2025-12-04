package com.be.server.core.admin.khachhang.repository;

import com.be.server.core.admin.khachhang.model.request.ADKhachHangSearchRequest;
import com.be.server.entity.KhachHang;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.KhachHangRepository;
import com.be.server.repository.SizeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ADKhachHangRepository extends KhachHangRepository {

    @Query(value = """
    SELECT hd
    FROM KhachHang hd
    WHERE (:q IS NULL OR :q = '' OR
           LOWER(hd.ma) LIKE LOWER(CONCAT('%', :q, '%')) OR
           LOWER(hd.ten) LIKE LOWER(CONCAT('%', :q, '%')) OR
           LOWER(hd.sdt) LIKE LOWER(CONCAT('%', :q, '%')))
    AND (:status IS NULL OR hd.status = :status)
""")
    Page<KhachHang> getAllKhachHang(Pageable pageable, @Param("q") String q, @Param("status") EntityStatus status);

    @Query(value = """
    SELECT hd.sdt
    FROM KhachHang hd
    WHERE  hd.sdt = :q
""")
    String checkSDTKhachHang(@Param("q") String q);


    @Query(value = """
    SELECT hd.cccd
    FROM KhachHang hd
    WHERE  hd.cccd = :q
""")
    String checkCCCDKhachHang(@Param("q") String q);
}
