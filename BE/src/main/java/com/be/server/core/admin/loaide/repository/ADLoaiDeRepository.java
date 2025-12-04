package com.be.server.core.admin.loaide.repository;

import com.be.server.entity.KhachHang;
import com.be.server.entity.LoaiDe;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.LoaiDeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ADLoaiDeRepository extends LoaiDeRepository {

    Page<LoaiDe> findByMaContainingOrTenContaining(String ma, String ten, Pageable pageable);


    @Query(value = """
    SELECT hd
    FROM LoaiDe hd
    WHERE (:q IS NULL OR :q = '' OR
           LOWER(hd.ma) LIKE LOWER(CONCAT('%', :q, '%')) OR
           LOWER(hd.ten) LIKE LOWER(CONCAT('%', :q, '%')))
    AND (:status IS NULL OR hd.status = :status)
    ORDER BY hd.createdDate DESC
""")
    Page<LoaiDe> getAllKhachHang(Pageable pageable, @Param("q") String q, @Param("status") EntityStatus status);

    List<LoaiDe> findByTen(String ten);

}
