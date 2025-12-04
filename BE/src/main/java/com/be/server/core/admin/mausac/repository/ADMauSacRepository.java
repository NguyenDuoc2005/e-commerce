package com.be.server.core.admin.mausac.repository;
import com.be.server.entity.DanhMuc;
import com.be.server.entity.MauSac;
import com.be.server.repository.MauSacRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ADMauSacRepository extends MauSacRepository {

    @Query("SELECT t FROM MauSac t WHERE (t.ma IS NULL OR t.ma LIKE %:search%) AND (t.ten IS NULL OR t.ten LIKE %:search%) ORDER BY t.createdDate DESC ")
    Page<MauSac> findByMaContainingOrTenContaining(@Param("search") String search, Pageable pageable);

    List<MauSac> findByTen(String ten);

    List<MauSac> findByMau(String mau);
}
