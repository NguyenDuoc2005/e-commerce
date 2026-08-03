package com.ecommerce.promotion.controller;

import com.ecommerce.common.util.ResponseUtils;
import com.ecommerce.promotion.model.request.VoucherRequest;
import com.ecommerce.promotion.model.request.VoucherSearchRequest;
import com.ecommerce.promotion.service.VoucherService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/voucher")
@CrossOrigin(origins = "*")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(VoucherSearchRequest request) {
        return ResponseUtils.createResponseEntity(voucherService.getAllVoucher(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(voucherService.getVoucherById(id));
    }

    @GetMapping("/listkh/{id}")
    public Page<String> getListKH(
            @PathVariable String id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return voucherService.getListKH(id, search, page, size);
    }

    @PostMapping
    public ResponseEntity<?> modify(@ModelAttribute VoucherRequest request) {
        return ResponseUtils.createResponseEntity(voucherService.modifyVoucher(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable String id) {
        return ResponseUtils.createResponseEntity(voucherService.changeVoucherStatus(id));
    }
}
