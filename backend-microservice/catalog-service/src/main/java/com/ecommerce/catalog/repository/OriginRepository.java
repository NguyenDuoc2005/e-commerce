package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Origin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OriginRepository extends JpaRepository<Origin, String> {
    List<Origin> findByName(String name);
}
