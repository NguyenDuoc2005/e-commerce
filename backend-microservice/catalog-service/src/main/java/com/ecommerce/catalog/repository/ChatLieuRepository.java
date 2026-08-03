package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatLieuRepository extends JpaRepository<ChatLieu, String> {
    List<ChatLieu> findByTen(String ten);
}
