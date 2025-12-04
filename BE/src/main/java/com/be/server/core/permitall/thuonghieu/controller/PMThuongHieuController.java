package com.be.server.core.permitall.thuonghieu.controller;

import com.be.server.core.permitall.sanpham.dto.request.PMSanPhamMoiSearchRequest;
import com.be.server.core.permitall.sanpham.service.PMSanPhamService;
import com.be.server.core.permitall.thuonghieu.dto.request.PMThuongHieuSearchRequest;
import com.be.server.core.permitall.thuonghieu.service.PMThuongHieuService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_PERMITALL_THUONGHIEU)
public class PMThuongHieuController {

    public final PMThuongHieuService pmThuongHieuService;

    @GetMapping("/get-all/thuong-hieu-trang-chu")
    public ResponseEntity<?> getAllSanPhamMoi(PMThuongHieuSearchRequest request) {
        return Helper.createResponseEntity(pmThuongHieuService.getAllThuongHieu(request));
    }

}
