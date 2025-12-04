package com.be.server.core.admin.xuatxu.repository;

import com.be.server.entity.XuatSu;
import com.be.server.repository.XuatSuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ADXuatXuRepository extends XuatSuRepository {

    Page<XuatSu> findByMaContainingOrTenContaining(String ma, String ten, Pageable pageable);

}
