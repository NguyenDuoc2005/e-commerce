package com.ecommerce.seller.service;

import com.ecommerce.seller.client.UserClient;
import com.ecommerce.seller.entity.ChatConversation;
import com.ecommerce.seller.entity.ChatMessage;
import com.ecommerce.seller.entity.Seller;
import com.ecommerce.seller.entity.SellerStatus;
import com.ecommerce.seller.repository.ChatConversationRepository;
import com.ecommerce.seller.repository.ChatMessageRepository;
import com.ecommerce.seller.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private static final String BUYER = "BUYER";
    private static final String SELLER = "SELLER";

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final SellerRepository sellerRepository;
    private final UserClient userClient;

    public ChatService(ChatConversationRepository conversationRepository, ChatMessageRepository messageRepository,
                       SellerRepository sellerRepository, UserClient userClient) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.sellerRepository = sellerRepository;
        this.userClient = userClient;
    }

    @Transactional
    public Map<String, Object> startBuyerConversation(String customerId, String sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .filter(value -> value.getStatus() == SellerStatus.APPROVED)
                .orElseThrow(() -> new IllegalArgumentException("Shop khong ton tai hoac chua duoc duyet"));
        ChatConversation conversation = conversationRepository.findByCustomerIdAndSellerId(customerId, sellerId)
                .orElseGet(() -> createConversation(customerId, seller));
        return conversationMap(conversation, BUYER);
    }

    public List<Map<String, Object>> buyerConversations(String customerId) {
        return conversationRepository.findByCustomerIdOrderByLastMessageAtDesc(customerId).stream()
                .map(row -> conversationMap(row, BUYER)).toList();
    }

    public List<Map<String, Object>> sellerConversations(String sellerId) {
        return conversationRepository.findBySellerIdOrderByLastMessageAtDesc(sellerId).stream()
                .map(row -> conversationMap(row, SELLER)).toList();
    }

    public List<Map<String, Object>> buyerMessages(String conversationId, String customerId) {
        ChatConversation conversation = requireBuyerConversation(conversationId, customerId);
        return messageMaps(conversation.getId());
    }

    public List<Map<String, Object>> sellerMessages(String conversationId, String sellerId) {
        ChatConversation conversation = requireSellerConversation(conversationId, sellerId);
        return messageMaps(conversation.getId());
    }

    @Transactional
    public Map<String, Object> sendBuyerMessage(String conversationId, String customerId, String content) {
        ChatConversation conversation = requireBuyerConversation(conversationId, customerId);
        return send(conversation, BUYER, customerId, content);
    }

    @Transactional
    public Map<String, Object> sendSellerMessage(String conversationId, String sellerId, String content) {
        ChatConversation conversation = requireSellerConversation(conversationId, sellerId);
        return send(conversation, SELLER, sellerId, content);
    }

    @Transactional
    public Map<String, Object> markBuyerRead(String conversationId, String customerId) {
        ChatConversation conversation = requireBuyerConversation(conversationId, customerId);
        messageRepository.markRead(conversationId, SELLER, Instant.now());
        conversation.setBuyerUnreadCount(0);
        conversationRepository.save(conversation);
        return conversationMap(conversation, BUYER);
    }

    @Transactional
    public Map<String, Object> markSellerRead(String conversationId, String sellerId) {
        ChatConversation conversation = requireSellerConversation(conversationId, sellerId);
        messageRepository.markRead(conversationId, BUYER, Instant.now());
        conversation.setSellerUnreadCount(0);
        conversationRepository.save(conversation);
        return conversationMap(conversation, SELLER);
    }

    private ChatConversation createConversation(String customerId, Seller seller) {
        ChatConversation conversation = new ChatConversation();
        conversation.setCustomerId(customerId);
        conversation.setSellerId(seller.getId());
        conversation.setBuyerName(buyerName(customerId));
        conversation.setShopName(seller.getShopName());
        conversation.setShopLogoUrl(seller.getLogoUrl());
        conversation.setBuyerUnreadCount(0);
        conversation.setSellerUnreadCount(0);
        return conversationRepository.save(conversation);
    }

    private Map<String, Object> send(ChatConversation conversation, String senderType, String senderId, String rawContent) {
        String content = rawContent == null ? "" : rawContent.trim();
        if (content.isBlank() || content.length() > 2000) {
            throw new IllegalArgumentException("Tin nhan phai co tu 1 den 2000 ky tu");
        }
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setContent(content);
        ChatMessage saved = messageRepository.save(message);

        conversation.setLastMessage(content.length() > 500 ? content.substring(0, 500) : content);
        conversation.setLastMessageAt(saved.getCreatedAt() == null ? Instant.now() : saved.getCreatedAt());
        if (BUYER.equals(senderType)) {
            conversation.setSellerUnreadCount(count(conversation.getSellerUnreadCount()) + 1);
        } else {
            conversation.setBuyerUnreadCount(count(conversation.getBuyerUnreadCount()) + 1);
        }
        conversationRepository.save(conversation);
        return messageMap(saved);
    }

    private ChatConversation requireBuyerConversation(String id, String customerId) {
        ChatConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay cuoc tro chuyen"));
        if (!conversation.getCustomerId().equals(customerId)) throw new SecurityException("Ban khong co quyen xem cuoc tro chuyen nay");
        return conversation;
    }

    private ChatConversation requireSellerConversation(String id, String sellerId) {
        ChatConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay cuoc tro chuyen"));
        if (!conversation.getSellerId().equals(sellerId)) throw new SecurityException("Cuoc tro chuyen khong thuoc shop nay");
        return conversation;
    }

    private List<Map<String, Object>> messageMaps(String conversationId) {
        List<ChatMessage> rows = new ArrayList<>(messageRepository.findTop100ByConversationIdOrderByCreatedAtDesc(conversationId));
        java.util.Collections.reverse(rows);
        return rows.stream().map(this::messageMap).toList();
    }

    private Map<String, Object> conversationMap(ChatConversation value, String viewerType) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", value.getId());
        row.put("customerId", value.getCustomerId());
        row.put("sellerId", value.getSellerId());
        row.put("buyerName", value.getBuyerName());
        row.put("shopName", value.getShopName());
        row.put("shopLogoUrl", value.getShopLogoUrl());
        row.put("lastMessage", value.getLastMessage());
        row.put("lastMessageAt", value.getLastMessageAt());
        row.put("unreadCount", BUYER.equals(viewerType) ? count(value.getBuyerUnreadCount()) : count(value.getSellerUnreadCount()));
        row.put("createdAt", value.getCreatedAt());
        return row;
    }

    private Map<String, Object> messageMap(ChatMessage value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", value.getId());
        row.put("conversationId", value.getConversationId());
        row.put("senderType", value.getSenderType());
        row.put("senderId", value.getSenderId());
        row.put("content", value.getContent());
        row.put("createdAt", value.getCreatedAt());
        row.put("readAt", value.getReadAt());
        return row;
    }

    private String buyerName(String customerId) {
        try {
            Object name = userClient.getCustomer(customerId).get("name");
            return name == null || String.valueOf(name).isBlank() ? "Nguoi mua" : String.valueOf(name);
        } catch (Exception ignored) {
            return "Nguoi mua";
        }
    }

    private int count(Integer value) { return value == null ? 0 : value; }
}
