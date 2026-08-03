package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.ChangeStatusRequest;
import com.ecommerce.order.model.request.HoaDonDetailRequest;
import com.ecommerce.order.model.request.HoaDonSearchRequest;
import com.ecommerce.order.model.request.ThanhToanRequest;
import com.ecommerce.order.service.AdminHoaDonService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/admin/hoa-don")
public class AdminHoaDonController {

    private final AdminHoaDonService adminHoaDonService;

    public AdminHoaDonController(AdminHoaDonService adminHoaDonService) {
        this.adminHoaDonService = adminHoaDonService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute HoaDonSearchRequest request) {
        return ResponseEntity.ok(adminHoaDonService.getAllHoaDon(request));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getHoaDonChiTiet(@ModelAttribute HoaDonDetailRequest request) {
        return ResponseEntity.ok(adminHoaDonService.getAllHoaDonChiTiet(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLichSuTrangThai(@PathVariable String id) {
        return ResponseEntity.ok(adminHoaDonService.getLichSuTrangThai(id));
    }

    @GetMapping("/lich_su_thanh_toan/{id}")
    public ResponseEntity<?> getLichSuThanhToan(@PathVariable String id) {
        return ResponseEntity.ok(adminHoaDonService.getLichSuThanhToan(id));
    }

    @PostMapping("/thanh_toan")
    public ResponseEntity<?> thanhToan(@ModelAttribute ThanhToanRequest request) {
        return ResponseEntity.ok(adminHoaDonService.thanhToanHoaDon(request));
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatus(@ModelAttribute ChangeStatusRequest request) {
        return ResponseEntity.ok(adminHoaDonService.changeStatus(request));
    }

    @GetMapping("/pdf/{maHoaDon}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable String maHoaDon) {
        byte[] pdfData = adminHoaDonService.generateInvoicePdf(maHoaDon);
        String encodedFileName = URLEncoder.encode("HoaDon_" + maHoaDon + ".pdf", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .body(pdfData);
    }

    @GetMapping("/delivery/{maHoaDon}/pdf")
    public ResponseEntity<byte[]> generateDeliveryPdf(@PathVariable String maHoaDon) {
        byte[] pdfData = adminHoaDonService.generateDeliveryPdf(maHoaDon);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"delivery_" + maHoaDon + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .body(pdfData);
    }
}
