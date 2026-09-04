package com.ecommerce.order.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="outbox", indexes=@Index(name="idx_outbox_status_created", columnList="status,created_at"))
public class OrderOutboxEvent {
 @Id @Column(length=36, updatable=false) private String id;
 @Column(name="aggregate_type", nullable=false) private String aggregateType;
 @Column(name="aggregate_id", nullable=false) private String aggregateId;
 @Column(name="event_type", nullable=false) private String eventType;
 @Lob @Column(name="payload", nullable=false) private String payload;
 @Column(name="event_key", nullable=false) private String eventKey;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private OutboxEventStatus status=OutboxEventStatus.PENDING;
 @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
 @Column(name="published_at") private Instant publishedAt;
 @PrePersist void init(){ if(id==null)id=UUID.randomUUID().toString(); if(createdAt==null)createdAt=Instant.now(); }
 public String getId(){return id;} public void setId(String v){id=v;}
 public String getAggregateType(){return aggregateType;} public void setAggregateType(String v){aggregateType=v;}
 public String getAggregateId(){return aggregateId;} public void setAggregateId(String v){aggregateId=v;}
 public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
 public String getPayload(){return payload;} public void setPayload(String v){payload=v;}
 public String getEventKey(){return eventKey;} public void setEventKey(String v){eventKey=v;}
 public OutboxEventStatus getStatus(){return status;} public void setStatus(OutboxEventStatus v){status=v;}
 public Instant getCreatedAt(){return createdAt;} public Instant getPublishedAt(){return publishedAt;} public void setPublishedAt(Instant v){publishedAt=v;}
}
