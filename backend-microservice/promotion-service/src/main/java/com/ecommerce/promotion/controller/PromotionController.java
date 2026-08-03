package com.ecommerce.promotion.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.promotion.entity.DotGiamGia;
import com.ecommerce.promotion.entity.SanPham;
import com.ecommerce.promotion.entity.SanPhamChiTiet;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;
import com.ecommerce.promotion.model.response.PromotionByProductDetail;
import com.ecommerce.promotion.repository.SanPhamChiTietRepository;
import com.ecommerce.promotion.repository.SanPhamRepository;
import com.ecommerce.promotion.repository.MauSacRepository;
import com.ecommerce.promotion.repository.KichCoRepository;
import com.ecommerce.promotion.service.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/dot-giam-gia")
public class PromotionController {

    private final PromotionService promotionService;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;

    public PromotionController(
            PromotionService promotionService,
            MauSacRepository mauSacRepository,
            KichCoRepository kichCoRepository
    ) {
        this.promotionService = promotionService;
        this.mauSacRepository = mauSacRepository;
        this.kichCoRepository = kichCoRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute FindPromotionRequest request) {
        return ResponseUtils.createResponseEntity(promotionService.getAll(request));
    }

    @GetMapping("/san-pham")
    public ResponseEntity<List<SanPham>> getSanPham() {
        return ResponseEntity.ok(promotionService.getSanPham());
    }

    @GetMapping("/san-pham-chi-tiet/{id}")
    public ResponseEntity<List<SanPhamChiTiet>> getSanPhamCT(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.getSanPhamCT(id));
    }

    @GetMapping("/san-pham-chi-tiet-by-dot/{id}")
    public ResponseEntity<List<SanPhamChiTiet>> getSanPhamCTByDot(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.getSanPhamByDot(id));
    }

    @GetMapping("/mau-sac")
    public ResponseEntity<?> getMauSac() {
        return ResponseEntity.ok(mauSacRepository.findAll());
    }

    @GetMapping("/size")
    public ResponseEntity<?> getSize() {
        return ResponseEntity.ok(kichCoRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<DotGiamGia> add(@RequestBody CreatePromotionRequest request) {
        return ResponseEntity.ok(promotionService.add(request));
    }

    @PostMapping("/expired/{id}")
    public ResponseEntity<DotGiamGia> markExpired(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.updateStatus(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DotGiamGia> update(@PathVariable String id, @RequestBody UpdatePromotionRequest request) {
        request.setId(id);
        return ResponseEntity.ok(promotionService.update(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionByIdResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.getByIdPromotion(id));
    }

    @GetMapping("/byProductDetail/{id}")
    public ResponseEntity<List<PromotionByProductDetail>> getByProductDetailId(@PathVariable String id) {
        return ResponseEntity.ok(promotionService.getByIdProductDetail(id));
    }
}
