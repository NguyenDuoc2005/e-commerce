package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.SoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoleTypeRepository extends JpaRepository<SoleType, String> {
    List<SoleType> findByName(String name);
}
