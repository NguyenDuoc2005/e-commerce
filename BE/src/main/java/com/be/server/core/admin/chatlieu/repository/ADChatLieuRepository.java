
package com.be.server.core.admin.chatlieu.repository;

import com.be.server.entity.ChatLieu;
import com.be.server.repository.ChatLieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ADChatLieuRepository extends ChatLieuRepository {

    @Query("SELECT t FROM ChatLieu t WHERE (t.ma IS NULL OR t.ma LIKE %:search%) AND (t.ten IS NULL OR t.ten LIKE %:search%) ORDER BY t.createdDate DESC ")
    Page<ChatLieu> findByMaContainingOrTenContaining(@Param("search") String search, Pageable pageable);



    List<ChatLieu> findByTen(String ten);

}
