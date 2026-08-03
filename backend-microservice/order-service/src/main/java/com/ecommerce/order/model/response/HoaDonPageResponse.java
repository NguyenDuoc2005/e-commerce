package com.ecommerce.order.model.response;

import com.ecommerce.order.constant.EntityTrangThaiHoaDon;
import org.springframework.data.domain.Page;

import java.util.Map;

public class HoaDonPageResponse {
    private Page<HoaDonResponse> page;
    private Map<EntityTrangThaiHoaDon, Long> countByStatus;

    public HoaDonPageResponse(Page<HoaDonResponse> page, Map<EntityTrangThaiHoaDon, Long> countByStatus) {
        this.page = page;
        this.countByStatus = countByStatus;
    }

    public Page<HoaDonResponse> getPage() { return page; }
    public void setPage(Page<HoaDonResponse> page) { this.page = page; }
    public Map<EntityTrangThaiHoaDon, Long> getCountByStatus() { return countByStatus; }
    public void setCountByStatus(Map<EntityTrangThaiHoaDon, Long> countByStatus) { this.countByStatus = countByStatus; }
}
