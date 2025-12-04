package com.be.server.core.admin.voucher.controller;

import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuRequest;
import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuSearchRequest;
import com.be.server.core.admin.voucher.model.request.ADVoucherRequest;
import com.be.server.core.admin.voucher.model.request.ADVoucherSearchRequest;
import com.be.server.core.admin.voucher.service.ADVoucherService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_VOUCHER)
@Slf4j
@CrossOrigin(origins = "*")
public class ADVoucherController {

    private final ADVoucherService advoucherService;


    @GetMapping
    public ResponseEntity<?> test(ADVoucherSearchRequest request) {
        return Helper.createResponseEntity(advoucherService.getAllVoucher(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(advoucherService.getVoucherById(id));
    }

    @GetMapping("/listkh/{id}")
    public Page<String> getListKHId(
            @PathVariable String id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return advoucherService.getListKH(id, search, page, size);
    }

    @PostMapping
    public ResponseEntity<?> modifyProduct(@ModelAttribute ADVoucherRequest request) {

        return Helper.createResponseEntity(advoucherService.modifyVoucher(request));
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return Helper.createResponseEntity(advoucherService.changeSizeStatus(id));
    }

}
