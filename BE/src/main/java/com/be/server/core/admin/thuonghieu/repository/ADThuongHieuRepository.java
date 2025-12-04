package com.be.server.core.admin.thuonghieu.repository;

import com.be.server.entity.ThuongHieu;
import com.be.server.repository.ThuongHieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADThuongHieuRepository extends ThuongHieuRepository {

    @Query("SELECT t FROM ThuongHieu t WHERE (t.ma IS NULL OR t.ma LIKE %:search%) AND (t.ten IS NULL OR t.ten LIKE %:search%) ORDER BY t.createdDate DESC ")
    Page<ThuongHieu> findByMaContainingOrTenContaining(@Param("search") String search, Pageable pageable);

    List<ThuongHieu> findByTen(String ten);

}
