package com.be.server.core.permitall.thuonghieu.repository;

import com.be.server.core.permitall.thuonghieu.dto.response.PMThuongHieuResponse;
import com.be.server.repository.ThuongHieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PMThuongHieuRepository extends ThuongHieuRepository {

    @Query("""
    SELECT th.id AS id, th.ma AS ma, th.ten AS ten
    FROM ThuongHieu th 
    WHERE th.status = 0
    order by th.createdDate desc
    """)
    Page<PMThuongHieuResponse> findAllThuongHieuInfo(Pageable pageable);


}
