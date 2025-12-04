package com.be.server.repository;

import com.be.server.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface SizeRepository extends JpaRepository<KichCo, String> {
    Collection<Object> findByTenContaining(String ten);
}
