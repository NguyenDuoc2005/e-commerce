package com.ecommerce.order.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.repository.OrderOutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;

@Service
public class OrderOutboxService {
 private final OrderOutboxEventRepository repo; private final ObjectMapper mapper;
 public OrderOutboxService(OrderOutboxEventRepository repo,ObjectMapper mapper){this.repo=repo;this.mapper=mapper;}
 @Transactional public OrderOutboxEvent append(String aggregateType,String aggregateId,String eventType,String key,Map<String,Object> payload){
  try { OrderOutboxEvent e=new OrderOutboxEvent(); e.setAggregateType(aggregateType);e.setAggregateId(aggregateId);e.setEventType(eventType);e.setEventKey(key);e.setPayload(mapper.writeValueAsString(payload));return repo.save(e); }
  catch(JsonProcessingException ex){throw new IllegalStateException("OUTBOX_PAYLOAD_INVALID",ex);}
 }
 @Transactional(readOnly=true) public java.util.List<OrderOutboxEvent> pending(){return repo.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);}
 @Transactional public void published(OrderOutboxEvent e){e.setStatus(OutboxEventStatus.PUBLISHED);e.setPublishedAt(Instant.now());repo.save(e);}
 @Transactional public void failed(OrderOutboxEvent e){e.setStatus(OutboxEventStatus.FAILED);repo.save(e);}
}
