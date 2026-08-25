package com.ecommerce.order.repository;

import com.ecommerce.order.entity.DisputeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisputeMessageRepository extends JpaRepository<DisputeMessage, String> {
    List<DisputeMessage> findByDisputeIdOrderByCreatedAtAsc(String disputeId);
}
