package com.ecommerce.payout.repository;
import com.ecommerce.payout.entity.ProcessedEvent; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent,String>{Optional<ProcessedEvent> findByEventKey(String key);}
