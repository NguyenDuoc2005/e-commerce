package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, String> {
    List<ThuongHieu> findByTen(String ten);
}
