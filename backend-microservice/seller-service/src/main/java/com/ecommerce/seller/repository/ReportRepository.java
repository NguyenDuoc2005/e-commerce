package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, String> {
    List<Report> findAllByOrderByCreatedAtDesc();
    boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatusIn(String reporterId, String targetType, String targetId, List<String> statuses);
}
