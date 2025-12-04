package com.be.server.core.permitall.donmua.controller;

import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.SanPhamChiTiet.service.ADSanPhamChiTietService;
import com.be.server.core.admin.banhang.model.request.ADThemSanPhamRequest;
import com.be.server.core.admin.hoadon.model.request.ADChangeStatusRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonDetailRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.admin.hoadon.service.ADHoaDonService;
import com.be.server.core.permitall.donmua.model.request.PMThemSanPhamRequest;
import com.be.server.core.permitall.donmua.model.request.UpdateDeliveryDTO;
import com.be.server.core.permitall.donmua.service.DonMuaService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping(MappingConstants.API_PERMITALL_ORDER_HISTORY)
public class DonMuaController {

    public final DonMuaService service;

    public final ADHoaDonService serviceHD;

    private final ADSanPhamChiTietService adSanPhamChiTietService;

    @GetMapping
    public ResponseEntity<?> getAll(ADHoaDonSearchRequest request) {
        return Helper.createResponseEntity(service.getAllHoaDon(request));
    }

    @GetMapping("/spct")
    public ResponseEntity<?> getALlSPCT(ADSPCTSearchRequest request) {

        return Helper.createResponseEntity(adSanPhamChiTietService.getAll(request));
    }

    @GetMapping("/all/{code}")
    public ResponseEntity<?> getAll1(@PathVariable String code) {
        return Helper.createResponseEntity(service.getAllHoaDon1(code));
    }

    @PostMapping("/sua-thong-tin")
    public ResponseEntity<?> suaThongTin(@ModelAttribute UpdateDeliveryDTO request) {
        return Helper.createResponseEntity(service.suaThongTin(request));
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatus(@ModelAttribute ADChangeStatusRequest adChangeStatusRequest) {
        return Helper.createResponseEntity(serviceHD.changeStatus(adChangeStatusRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getHDCT(@ModelAttribute ADHoaDonDetailRequest request) {
        return Helper.createResponseEntity(serviceHD.getAllHoaDonCT1(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLSHD(@PathVariable String id) {
        return Helper.createResponseEntity(serviceHD.getLSTTHD(id));
    }

    @PostMapping("them-san-pham")
    public ResponseEntity<?> modifyProduct(@ModelAttribute PMThemSanPhamRequest request) {
        return Helper.createResponseEntity(service.createThemSanPham(request));
    }

    @GetMapping("/lich_su_thanh_toan/{id}")
    public ResponseEntity<?> getLSTT(@PathVariable String id) {
        return Helper.createResponseEntity(service.getLSTT(id));
    }
}
