package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Color;
import com.ecommerce.catalog.constant.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColorRepository extends JpaRepository<Color, String> {
    List<Color> findByName(String name);
    List<Color> findByMau(String mau);
    List<Color> findByStatusOrderByCreatedDateDesc(EntityStatus status);
}
