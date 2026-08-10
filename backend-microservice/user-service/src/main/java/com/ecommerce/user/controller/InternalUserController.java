package com.ecommerce.user.controller;

import com.ecommerce.user.repository.KhachHangRepository;
import com.ecommerce.user.entity.KhachHang;
import com.ecommerce.user.entity.NhanVien;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.repository.NhanVienRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;

    public InternalUserController(KhachHangRepository khachHangRepository, NhanVienRepository nhanVienRepository) {
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    @GetMapping("/customers/{id}")
    public Map<String, Object> getCustomer(@PathVariable String id) {
        return khachHangRepository.findById(id)
                .map(kh -> {
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("id", kh.getId());
                    row.put("ma", kh.getMa());
                    row.put("ten", kh.getTen());
                    row.put("email", kh.getEmail());
                    row.put("sdt", kh.getSdt());
                    row.put("diaChi", kh.getDiaChi());
                    row.put("status", kh.getStatus() == null ? null : kh.getStatus().name());
                    return row;
                })
                .orElseGet(Map::of);
    }

    @GetMapping("/customers")
    public List<Map<String, Object>> searchCustomers(@RequestParam(required = false) String q) {
        String normalizedQ = q == null ? "" : q.replace("%", "").toLowerCase();
        return khachHangRepository.findAll().stream()
                .filter(kh -> kh.getStatus() == EntityStatus.ACTIVE)
                .filter(kh -> normalizedQ.isBlank()
                        || contains(kh.getTen(), normalizedQ)
                        || contains(kh.getMa(), normalizedQ)
                        || contains(kh.getSdt(), normalizedQ))
                .map(this::customerMap)
                .toList();
    }

    @PostMapping("/customers")
    public Map<String, Object> createCustomer(@RequestParam String ten, @RequestParam String sdt) {
        KhachHang khachHang = new KhachHang();
        khachHang.setTen(ten);
        khachHang.setSdt(sdt);
        khachHang.setStatus(EntityStatus.ACTIVE);
        return customerMap(khachHangRepository.save(khachHang));
    }

    @GetMapping("/auth/customers/by-email")
    public Map<String, Object> getAuthCustomerByEmail(@RequestParam String email, @RequestParam(defaultValue = "false") boolean activeOnly) {
        return (activeOnly ? khachHangRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE) : khachHangRepository.findByEmail(email))
                .map(this::authCustomerMap)
                .orElseGet(Map::of);
    }

    @GetMapping("/auth/customers/by-phone")
    public Map<String, Object> getAuthCustomerByPhone(@RequestParam String phone) {
        return khachHangRepository.findBySdt(phone).map(this::authCustomerMap).orElseGet(Map::of);
    }

    @PostMapping("/auth/customers")
    public Map<String, Object> createAuthCustomer(@RequestParam String ten, @RequestParam String email, @RequestParam String sdt, @RequestParam String matKhau) {
        KhachHang khachHang = new KhachHang();
        khachHang.setTen(ten);
        khachHang.setEmail(email);
        khachHang.setSdt(sdt);
        khachHang.setMatKhau(matKhau);
        khachHang.setStatus(EntityStatus.ACTIVE);
        return authCustomerMap(khachHangRepository.save(khachHang));
    }

    @PostMapping("/auth/customers/password")
    public void updateCustomerPassword(@RequestParam String email, @RequestParam String matKhau) {
        khachHangRepository.findByEmail(email).ifPresent(kh -> {
            kh.setMatKhau(matKhau);
            khachHangRepository.save(kh);
        });
    }

    @GetMapping("/auth/staff/by-email")
    public Map<String, Object> getAuthStaffByEmail(@RequestParam String email, @RequestParam(defaultValue = "false") boolean activeOnly) {
        return (activeOnly ? nhanVienRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE) : nhanVienRepository.findByEmail(email))
                .map(this::authStaffMap)
                .orElseGet(Map::of);
    }

    private Map<String, Object> customerMap(KhachHang kh) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", kh.getId());
        row.put("ma", kh.getMa());
        row.put("ten", kh.getTen());
        row.put("email", kh.getEmail());
        row.put("sdt", kh.getSdt());
        row.put("avatar", kh.getAvatar());
        row.put("diaChi", kh.getDiaChi());
        row.put("tinh", kh.getTinh());
        row.put("huyen", kh.getHuyen());
        row.put("xa", kh.getXa());
        row.put("status", kh.getStatus() == null ? null : kh.getStatus().name());
        return row;
    }

    private Map<String, Object> authCustomerMap(KhachHang kh) {
        Map<String, Object> row = customerMap(kh);
        row.put("matKhau", kh.getMatKhau());
        return row;
    }

    private Map<String, Object> authStaffMap(NhanVien nv) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", nv.getId());
        row.put("ma", nv.getMa());
        row.put("ten", nv.getTen());
        row.put("email", nv.getEmail());
        row.put("avatar", nv.getAvatar());
        row.put("matKhau", nv.getMatKhau());
        row.put("role", nv.getChucVu() == null ? null : nv.getChucVu().name());
        row.put("status", nv.getStatus() == null ? null : nv.getStatus().name());
        return row;
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }
}
