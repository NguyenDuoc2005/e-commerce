package com.be.server.core.admin.hoadon.controller;

import com.be.server.core.admin.hoadon.model.request.ADChangeStatusRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonDetailRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.admin.hoadon.model.request.ThanhToanRequest;
import com.be.server.core.admin.hoadon.service.ADHoaDonService;
import com.be.server.core.admin.hoadon.service.PDFDeliveryService;
import com.be.server.core.admin.hoadon.service.PDFInvoiceService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_HOA_DON)
@Slf4j
@CrossOrigin(origins = "*")
public class ADHoaDonController {

    public final ADHoaDonService service;

    private final PDFInvoiceService pdfInvoiceService;

    private final PDFDeliveryService pdfDeliveryService;

    @GetMapping
    public ResponseEntity<?> getAll(ADHoaDonSearchRequest request) {
        return Helper.createResponseEntity(service.getAllHoaDon(request));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getHDCT(@ModelAttribute ADHoaDonDetailRequest request) {
        return Helper.createResponseEntity(service.getAllHoaDonCT(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLSHD(@PathVariable String id) {
        return Helper.createResponseEntity(service.getLSTTHD(id));
    }

    @GetMapping("/lich_su_thanh_toan/{id}")
    public ResponseEntity<?> getLSTT(@PathVariable String id) {
        return Helper.createResponseEntity(service.getLSTT(id));
    }

    @PostMapping("/thanh_toan")
    public ResponseEntity<?> thanhToan(@ModelAttribute ThanhToanRequest thanhToanRequest) {
        return Helper.createResponseEntity(service.thanhToanHoaDon(thanhToanRequest));
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatus(@ModelAttribute ADChangeStatusRequest adChangeStatusRequest) {
        return Helper.createResponseEntity(service.changeStatus(adChangeStatusRequest));
    }

    @GetMapping("/pdf/{maHoaDon}")
    public ResponseEntity<?> downloadInvoicePDF(@PathVariable String maHoaDon) {
        try {
            log.info("Bắt đầu tạo PDF cho hóa đơn: {}", maHoaDon);

            // Tạo PDF
            byte[] pdfData = pdfInvoiceService.generateInvoicePDF(maHoaDon);

            // Tạo tên file với encoding UTF-8
            String fileName = "HoaDon_" + maHoaDon + ".pdf";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.setPragma("public");
            headers.setContentLength(pdfData.length);

            log.info("Tạo PDF thành công cho hóa đơn: {}, kích thước: {} bytes", maHoaDon, pdfData.length);

            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Lỗi khi tạo PDF cho hóa đơn: {}", maHoaDon, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/delivery/{maHoaDon}/pdf")
    public ResponseEntity<byte[]> generateDeliveryPDF(@PathVariable String maHoaDon) {
        try {
            byte[] pdfBytes = pdfDeliveryService.generateDeliveryPDF(maHoaDon);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "delivery_" + maHoaDon + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}