package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanhMucRepository extends JpaRepository<DanhMuc, String> {
    List<DanhMuc> findByTen(String ten);
}
