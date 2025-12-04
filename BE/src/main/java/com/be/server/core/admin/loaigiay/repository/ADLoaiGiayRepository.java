package com.be.server.core.admin.loaigiay.repository;

import com.be.server.entity.DanhMuc;
import com.be.server.repository.DanhMucRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADLoaiGiayRepository extends DanhMucRepository {

    @Query("SELECT t FROM DanhMuc t WHERE (t.ma IS NULL OR t.ma LIKE %:search%) AND (t.ten IS NULL OR t.ten LIKE %:search%) ORDER BY t.createdDate DESC ")
    Page<DanhMuc> findByMaContainingOrTenContaining(@Param("search") String search, Pageable pageable);


    List<DanhMuc> findByTen(String ten);


}
