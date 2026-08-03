package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MauSacRepository extends JpaRepository<MauSac, String> {
    List<MauSac> findByTen(String ten);
    List<MauSac> findByMau(String mau);
}
