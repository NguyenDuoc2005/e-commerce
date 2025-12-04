package com.be.server.core.admin.hoadon.repository;

import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.admin.hoadon.model.response.ADHoaDonChiTietResponseDetail;
import com.be.server.core.admin.hoadon.model.response.HoaDonPageResponse;
import com.be.server.core.admin.hoadon.model.response.HoaDonPageResponse1;
import com.be.server.entity.HoaDon;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ADHoaDonRepositoryCustom {
    HoaDonPageResponse getAllHoaDonResponse(ADHoaDonSearchRequest request, Pageable pageable);

    HoaDonPageResponse1 getAllHoaDonResponse1(ADHoaDonSearchRequest request);

    HoaDonPageResponse1 getAllHoaDonResponse1(String code);

    List<ADHoaDonChiTietResponseDetail> getAllHoaDonChiTietResponse(String maHoaDon);

    HoaDon updateTrangThaiHoaDon(String maHoaDon, EntityTrangThaiHoaDon TrangThai);
}