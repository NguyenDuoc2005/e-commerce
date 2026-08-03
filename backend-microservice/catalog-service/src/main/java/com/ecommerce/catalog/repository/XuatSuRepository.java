package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.XuatSu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XuatSuRepository extends JpaRepository<XuatSu, String> {
    List<XuatSu> findByTen(String ten);
}
