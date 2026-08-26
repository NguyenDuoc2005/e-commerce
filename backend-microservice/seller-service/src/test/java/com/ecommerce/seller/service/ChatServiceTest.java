package com.ecommerce.seller.service;

import com.ecommerce.seller.client.UserClient;
import com.ecommerce.seller.entity.ChatConversation;
import com.ecommerce.seller.entity.ChatMessage;
import com.ecommerce.seller.entity.Seller;
import com.ecommerce.seller.entity.SellerStatus;
import com.ecommerce.seller.repository.ChatConversationRepository;
import com.ecommerce.seller.repository.ChatMessageRepository;
import com.ecommerce.seller.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock ChatConversationRepository conversationRepository;
    @Mock ChatMessageRepository messageRepository;
    @Mock SellerRepository sellerRepository;
    @Mock UserClient userClient;
    ChatService service;

    @BeforeEach
    void setUp() {
        service = new ChatService(conversationRepository, messageRepository, sellerRepository, userClient);
        org.mockito.Mockito.lenient().when(conversationRepository.save(any())).thenAnswer(invocation -> {
            ChatConversation value = invocation.getArgument(0);
            if (value.getId() == null) value.setId("conversation-1");
            if (value.getCreatedAt() == null) value.setCreatedAt(Instant.now());
            if (value.getLastMessageAt() == null) value.setLastMessageAt(Instant.now());
            return value;
        });
    }

    @Test
    void buyerStartsOneConversationPerApprovedShop() {
        when(sellerRepository.findById("seller-1")).thenReturn(Optional.of(seller()));
        when(conversationRepository.findByCustomerIdAndSellerId("buyer-1", "seller-1")).thenReturn(Optional.empty());
        when(userClient.getCustomer("buyer-1")).thenReturn(Map.of("name", "Nguyen Van Buyer"));

        Map<String, Object> result = service.startBuyerConversation("buyer-1", "seller-1");

        assertEquals("conversation-1", result.get("id"));
        assertEquals("Nguyen Van Buyer", result.get("buyerName"));
        assertEquals("Demo Shop", result.get("shopName"));
    }

    @Test
    void messagesIncrementReceiverUnreadAndCanBeMarkedRead() {
        ChatConversation conversation = conversation();
        when(conversationRepository.findById("conversation-1")).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any())).thenAnswer(invocation -> {
            ChatMessage value = invocation.getArgument(0); value.setId("message-1"); value.setCreatedAt(Instant.now()); return value;
        });

        Map<String, Object> sent = service.sendBuyerMessage("conversation-1", "buyer-1", "  Shop con hang khong?  ");

        assertEquals("Shop con hang khong?", sent.get("content"));
        assertEquals(1, conversation.getSellerUnreadCount());
        service.markSellerRead("conversation-1", "seller-1");
        assertEquals(0, conversation.getSellerUnreadCount());
        verify(messageRepository).markRead(any(), any(), any());
    }

    @Test
    void sellerCannotReadAnotherShopsConversation() {
        when(conversationRepository.findById("conversation-1")).thenReturn(Optional.of(conversation()));
        assertThrows(SecurityException.class, () -> service.sellerMessages("conversation-1", "seller-2"));
    }

    private Seller seller() {
        Seller value = new Seller(); value.setId("seller-1"); value.setOwnerCustomerId("owner-1");
        value.setShopName("Demo Shop"); value.setSellerSlug("demo-shop"); value.setStatus(SellerStatus.APPROVED); return value;
    }
    private ChatConversation conversation() {
        ChatConversation value = new ChatConversation(); value.setId("conversation-1"); value.setCustomerId("buyer-1");
        value.setSellerId("seller-1"); value.setBuyerName("Buyer"); value.setShopName("Demo Shop");
        value.setBuyerUnreadCount(0); value.setSellerUnreadCount(0); value.setLastMessageAt(Instant.now()); return value;
    }
}
