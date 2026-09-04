package com.ecommerce.order.repository;
import com.ecommerce.order.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent,String>{
 List<OrderOutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
