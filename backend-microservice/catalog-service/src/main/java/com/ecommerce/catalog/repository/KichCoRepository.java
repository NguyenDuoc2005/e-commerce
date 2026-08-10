package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.KichCo;
import com.ecommerce.catalog.constant.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KichCoRepository extends JpaRepository<KichCo, String> {
    List<KichCo> findByTenContaining(String ten);
    List<KichCo> findByStatusOrderByCreatedDateDesc(EntityStatus status);
}
