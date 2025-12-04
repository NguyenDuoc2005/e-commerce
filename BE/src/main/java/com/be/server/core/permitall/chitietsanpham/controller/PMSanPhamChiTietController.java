package com.be.server.core.permitall.chitietsanpham.controller;

import com.be.server.core.permitall.chitietsanpham.dto.request.PMSanPhamChiTietRequest;
import com.be.server.core.permitall.chitietsanpham.service.PMSanPhamChiTietService;
import com.be.server.core.permitall.sanpham.dto.request.PMSanPhamMoiSearchRequest;
import com.be.server.core.permitall.sanpham.service.PMSanPhamService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_PERMITALL_SANPHAM_CHITIET)
public class PMSanPhamChiTietController {

    public final PMSanPhamChiTietService pmSanPhamChiTietService;

    @GetMapping("/get-all/san-pham-chi-tiet")
    public ResponseEntity<?> getAllSanPhamGiamGia(PMSanPhamChiTietRequest request) {
        return Helper.createResponseEntity(pmSanPhamChiTietService.getSanPhamChiTiet(request));
    }

}
