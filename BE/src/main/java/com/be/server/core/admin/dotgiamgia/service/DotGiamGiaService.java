package com.be.server.core.admin.dotgiamgia.service;

import com.be.server.core.admin.dotgiamgia.model.request.CreateDotGiamGiaRequest;
import com.be.server.core.admin.dotgiamgia.model.request.FindDotGiamGiaRequest;
import com.be.server.core.admin.dotgiamgia.model.request.UpdateDotGiamGiaRequest;
import com.be.server.core.admin.dotgiamgia.model.response.DotGiamGiaByIdResponse;
import com.be.server.core.admin.dotgiamgia.model.response.PromotionByProDuctDetail;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.DotGiamGia;
import com.be.server.entity.SanPham;
import com.be.server.entity.SanPhamChiTiet;

import java.util.List;

public interface DotGiamGiaService {

    ResponseObject<?> getAll(FindDotGiamGiaRequest findDotGiamGiaRequest);

    List<SanPham> getSanPham();

    List<SanPhamChiTiet> getSanPhamCT(String id);

    List<SanPhamChiTiet> getSanPhamByDot(String id);

    DotGiamGia add(CreateDotGiamGiaRequest createDotGiamGiaRequest) throws Exception;

    DotGiamGia update(UpdateDotGiamGiaRequest request);

    DotGiamGia updateStatus(String id);

    DotGiamGiaByIdResponse getByIdPromotion(String id);

    List<PromotionByProDuctDetail> getByIdProductDetail(String id);
}