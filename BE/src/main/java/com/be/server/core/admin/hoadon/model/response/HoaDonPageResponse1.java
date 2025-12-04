package com.be.server.core.admin.hoadon.model.response;

import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HoaDonPageResponse1 {

    private List<ADHoaDonResponse1> page;

    private Long totalRecords;

    private Map<EntityTrangThaiHoaDon, Long> countByStatus;
}