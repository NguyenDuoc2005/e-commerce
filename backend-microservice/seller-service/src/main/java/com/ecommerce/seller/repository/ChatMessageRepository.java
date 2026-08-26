package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findTop100ByConversationIdOrderByCreatedAtDesc(String conversationId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.readAt = :readAt WHERE m.conversationId = :conversationId AND m.senderType = :senderType AND m.readAt IS NULL")
    int markRead(@Param("conversationId") String conversationId, @Param("senderType") String senderType,
                 @Param("readAt") Instant readAt);
}
