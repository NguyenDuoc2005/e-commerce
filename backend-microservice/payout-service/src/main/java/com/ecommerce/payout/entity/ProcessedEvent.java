package com.ecommerce.payout.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="processed_event", uniqueConstraints=@UniqueConstraint(name="uk_processed_event_key",columnNames="event_key"))
public class ProcessedEvent { @Id @Column(length=36) private String id; @Column(name="event_key",nullable=false) private String eventKey; @Column(name="event_type",nullable=false) private String eventType; @Column(name="processed_at",nullable=false) private Instant processedAt;
 @PrePersist void init(){if(id==null)id=UUID.randomUUID().toString();if(processedAt==null)processedAt=Instant.now();}
 public String getId(){return id;} public String getEventKey(){return eventKey;} public void setEventKey(String v){eventKey=v;} public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
}
