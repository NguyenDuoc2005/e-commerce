package com.ecommerce.seller.service;

import com.ecommerce.seller.entity.PlatformBanner;
import com.ecommerce.seller.model.PlatformBannerRequest;
import com.ecommerce.seller.repository.PlatformBannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformBannerService {

    private final PlatformBannerRepository repository;

    public PlatformBannerService(PlatformBannerRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> publicBanners(String position) {
        Instant now = Instant.now();
        return repository.findAllByOrderBySortOrderAscCreatedAtDesc().stream()
                .filter(banner -> Boolean.TRUE.equals(banner.getActive()))
                .filter(banner -> position == null || position.isBlank() || position.equalsIgnoreCase(banner.getPosition()))
                .filter(banner -> banner.getStartAt() == null || !banner.getStartAt().isAfter(now))
                .filter(banner -> banner.getEndAt() == null || !banner.getEndAt().isBefore(now))
                .map(this::toMap)
                .toList();
    }

    public List<Map<String, Object>> list() {
        return repository.findAllByOrderBySortOrderAscCreatedAtDesc().stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> create(PlatformBannerRequest request) {
        PlatformBanner banner = new PlatformBanner();
        apply(banner, request);
        return toMap(repository.save(banner));
    }

    @Transactional
    public Map<String, Object> update(String id, PlatformBannerRequest request) {
        PlatformBanner banner = requireBanner(id);
        apply(banner, request);
        return toMap(repository.save(banner));
    }

    @Transactional
    public void delete(String id) {
        repository.delete(requireBanner(id));
    }

    private PlatformBanner requireBanner(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay banner"));
    }

    private void apply(PlatformBanner banner, PlatformBannerRequest request) {
        if (request.getStartAt() != null && request.getEndAt() != null && request.getEndAt().isBefore(request.getStartAt())) {
            throw new IllegalArgumentException("Thoi gian ket thuc phai sau thoi gian bat dau");
        }
        banner.setTitle(request.getTitle().trim());
        banner.setImageUrl(request.getImageUrl().trim());
        banner.setTargetUrl(blankToNull(request.getTargetUrl()));
        banner.setPosition(request.getPosition() == null || request.getPosition().isBlank()
                ? "HOME_HERO" : request.getPosition().trim().toUpperCase());
        banner.setActive(request.getActive() == null ? true : request.getActive());
        banner.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        banner.setStartAt(request.getStartAt());
        banner.setEndAt(request.getEndAt());
    }

    private Map<String, Object> toMap(PlatformBanner banner) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", banner.getId());
        row.put("title", banner.getTitle());
        row.put("imageUrl", banner.getImageUrl());
        row.put("targetUrl", banner.getTargetUrl());
        row.put("position", banner.getPosition());
        row.put("active", banner.getActive());
        row.put("sortOrder", banner.getSortOrder());
        row.put("startAt", banner.getStartAt());
        row.put("endAt", banner.getEndAt());
        row.put("createdAt", banner.getCreatedAt());
        row.put("updatedAt", banner.getUpdatedAt());
        return row;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
