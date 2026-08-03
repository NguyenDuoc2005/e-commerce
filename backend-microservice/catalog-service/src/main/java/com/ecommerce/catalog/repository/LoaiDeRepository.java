package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.LoaiDe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoaiDeRepository extends JpaRepository<LoaiDe, String> {
    List<LoaiDe> findByTen(String ten);
}
