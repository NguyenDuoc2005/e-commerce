package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.PlatformBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformBannerRepository extends JpaRepository<PlatformBanner, String> {
    List<PlatformBanner> findAllByOrderBySortOrderAscCreatedAtDesc();
}
