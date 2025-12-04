package com.be.server.repository;

import com.be.server.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ChatLieuRepository extends JpaRepository<ChatLieu, String> {
}
