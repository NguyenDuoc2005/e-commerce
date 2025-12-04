package com.be.server.core.admin.SanPhamChiTiet.controller;


import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTRequest;
import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.SanPhamChiTiet.service.ADSanPhamChiTietService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_SAN_PHAM_CHI_TIET)
@Slf4j
@CrossOrigin(origins = "*")
public class SanPhamChiTietController {

    private final ADSanPhamChiTietService adSanPhamChiTietService;

    @GetMapping
    public ResponseEntity<?> getALl(ADSPCTSearchRequest request) {

        return Helper.createResponseEntity(adSanPhamChiTietService.getAll(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(adSanPhamChiTietService.changeSanPhamStatus(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(adSanPhamChiTietService.getSPCTById(id));

    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetailSPCT(@PathVariable String id) {

        return Helper.createResponseEntity(adSanPhamChiTietService.getDetailSPCT(id));

    }


    @GetMapping("/list-mau")
    public ResponseEntity<?> getListColor() {

        return Helper.createResponseEntity(adSanPhamChiTietService.getListColor());

    }

    @GetMapping("/list-size")
    public ResponseEntity<?> getListSize() {

        return Helper.createResponseEntity(adSanPhamChiTietService.getListSize());

    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADSPCTRequest request) {

        return Helper.createResponseEntity(adSanPhamChiTietService.modifySanPham(request));
    }

    @PostMapping("update")
    public ResponseEntity<?> updateProduct(@ModelAttribute ADSPCTRequest request) {

        return Helper.createResponseEntity(adSanPhamChiTietService.updateSanPham(request));
    }


    @GetMapping("/list-sp")
    public ResponseEntity<?> getListSP() {

        return Helper.createResponseEntity(adSanPhamChiTietService.getListThemSanPham());

    }


}
