package com.be.server.core.admin.dotgiamgia.controller;

import com.be.server.core.admin.dotgiamgia.model.request.CreateDotGiamGiaRequest;
import com.be.server.core.admin.dotgiamgia.model.request.FindDotGiamGiaRequest;
import com.be.server.core.admin.dotgiamgia.model.request.UpdateDotGiamGiaRequest;
import com.be.server.core.admin.dotgiamgia.model.response.DotGiamGiaByIdResponse;
import com.be.server.core.admin.dotgiamgia.model.response.PromotionByProDuctDetail;
import com.be.server.core.admin.dotgiamgia.service.DotGiamGiaService;
import com.be.server.entity.*;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.repository.MauSacRepository;
import com.be.server.repository.SanPhamChiTietRepository;
import com.be.server.repository.SanPhamRepository;
import com.be.server.repository.SizeRepository;
import com.be.server.utils.Helper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping(MappingConstants.API_ADMIN_DOT_GIAM_GIA)
public class ADDotGiamGiaRestController {

    @Autowired private DotGiamGiaService dotGiamGiaService;
    @Autowired private MauSacRepository mauSacRepository;
    @Autowired private SizeRepository sizeRepository;

    @GetMapping()
    public ResponseEntity<?> getAll(@ModelAttribute final FindDotGiamGiaRequest findDotGiamGiaRequest) {
        return Helper.createResponseEntity(dotGiamGiaService.getAll(findDotGiamGiaRequest));
    }

    @GetMapping("/san-pham")
    public ResponseEntity<?> getSanPham() {
        List<SanPham> sanPhams = dotGiamGiaService.getSanPham();
        return ResponseEntity.ok(sanPhams);
    }

    @GetMapping("/san-pham-chi-tiet/{id}")
    public ResponseEntity<?> getSanPhamCT(@PathVariable String id) {
        List<SanPhamChiTiet> sanPhamsTiets = dotGiamGiaService.getSanPhamCT(id);
        return ResponseEntity.ok(sanPhamsTiets);
    }

    @GetMapping("/san-pham-chi-tiet-by-dot/{id}")
    public ResponseEntity<?> getSanPhamCTByDot(@PathVariable String id) {
        List<SanPhamChiTiet> sanPhamsTiets = dotGiamGiaService.getSanPhamByDot(id);
        return ResponseEntity.ok(sanPhamsTiets);
    }


    @GetMapping("/mau-sac")
    public ResponseEntity<?> getMauSac() {
        List<MauSac> list = mauSacRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/size")
    public ResponseEntity<?> getSize() {
        List<KichCo> list = sizeRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody CreateDotGiamGiaRequest request,
                                 BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException("Validation failed: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        DotGiamGia dotGiamGia = dotGiamGiaService.add(request);
        return ResponseEntity.ok(dotGiamGia);
    }

    @PostMapping("/expired/{id}")
    public ResponseEntity<?> markExpired(@PathVariable("id") String id) {
        DotGiamGia dotGiamGia = dotGiamGiaService.updateStatus(id);
        return ResponseEntity.ok(dotGiamGia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id,
                                    @RequestBody UpdateDotGiamGiaRequest request) {
        request.setId(id);
        DotGiamGia dotGiamGia = dotGiamGiaService.update(request);
        return ResponseEntity.ok(dotGiamGia);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") String id) {
        DotGiamGiaByIdResponse dotGiamGiaByIdResponse = dotGiamGiaService.getByIdPromotion(id);
        return ResponseEntity.ok(dotGiamGiaByIdResponse);
    }

    @GetMapping("/byProductDetail/{id}")
    public ResponseEntity<?> getByProductDetailId(@PathVariable("id") String id) {
        List<PromotionByProDuctDetail> byIdProductDetail = dotGiamGiaService.getByIdProductDetail(id);
        return ResponseEntity.ok(byIdProductDetail);
    }
}