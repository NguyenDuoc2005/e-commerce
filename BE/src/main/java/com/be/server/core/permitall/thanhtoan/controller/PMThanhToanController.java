package com.be.server.core.permitall.thanhtoan.controller;

import com.be.server.core.admin.khachhang.service.ADKhachHangService;
import com.be.server.core.admin.nhanvien.service.ADNhanVienService;
import com.be.server.core.permitall.thanhtoan.model.request.PMPhieuGiamGia;
import com.be.server.core.permitall.thanhtoan.model.request.PMThanhToan;
import com.be.server.core.permitall.thanhtoan.service.Impl.thanhtoanserviceImpl;
import com.be.server.entity.HoaDon;
import com.be.server.utils.Helper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class PMThanhToanController {

    @Autowired
    private final thanhtoanserviceImpl orderService;

    public final ADKhachHangService service;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody PMThanhToan order, HttpServletRequest request) {

        if ("VNPAY".equals(order.getHinhThucThanhToan())) {
            String ipAddr = request.getRemoteAddr();
            Map<String, String> vnpayResponse = orderService.createVNPayPaymentUrl(order, ipAddr);
            return ResponseEntity.ok(vnpayResponse);
        } else {
            HoaDon savedOrder = orderService.createOrder(order);
            return ResponseEntity.ok(savedOrder);
        }

    }

    @GetMapping("/vnpay-return")
    public void handleVNPayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isSuccess = orderService.handleVNPayReturn(request);
        if (isSuccess) {
            response.sendRedirect("http://localhost:6688/thanh-toan-thanh-cong");
        } else {
            response.sendRedirect("http://localhost:6688/trang-chu");
        }
    }

    @PostMapping("/pgg")
    public ResponseEntity<?> getPGG(PMPhieuGiamGia req) {
        return ResponseEntity.ok(orderService.getPhieuGiamGia(req));
    }

    @PostMapping("/pgg/list")
    public ResponseEntity<?> getAllApplicablePGG(PMPhieuGiamGia req) {

        System.out.println(req.getTongTien());

        return ResponseEntity.ok(orderService.getAllApplicablePGG(req.getIdKH(), req.getTongTien()));
    }

    @PostMapping("/khach-hang/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {

        return Helper.createResponseEntity(service.getKhachHangById(id));
    }

}