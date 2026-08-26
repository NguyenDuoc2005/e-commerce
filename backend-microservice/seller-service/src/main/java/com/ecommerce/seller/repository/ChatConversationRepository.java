package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, String> {
    Optional<ChatConversation> findByCustomerIdAndSellerId(String customerId, String sellerId);
    List<ChatConversation> findByCustomerIdOrderByLastMessageAtDesc(String customerId);
    List<ChatConversation> findBySellerIdOrderByLastMessageAtDesc(String sellerId);
}
