package com.ecommerce.user.controller;

import com.ecommerce.user.repository.CustomerRepository;
import com.ecommerce.user.entity.Customer;
import com.ecommerce.user.entity.Staff;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.repository.StaffRepository;
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

    private final CustomerRepository khachHangRepository;
    private final StaffRepository nhanVienRepository;

    public InternalUserController(CustomerRepository khachHangRepository, StaffRepository nhanVienRepository) {
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    @GetMapping("/customers/{id}")
    public Map<String, Object> getCustomer(@PathVariable String id) {
        return khachHangRepository.findById(id)
                .map(kh -> {
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("id", kh.getId());
                    row.put("code", kh.getCode());
                    row.put("name", kh.getName());
                    row.put("email", kh.getEmail());
                    row.put("phoneNumber", kh.getPhoneNumber());
                    row.put("address", kh.getAddress());
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
                        || contains(kh.getName(), normalizedQ)
                        || contains(kh.getCode(), normalizedQ)
                        || contains(kh.getPhoneNumber(), normalizedQ))
                .map(this::customerMap)
                .toList();
    }

    @PostMapping("/customers")
    public Map<String, Object> createCustomer(@RequestParam String name, @RequestParam String phoneNumber) {
        Customer khachHang = new Customer();
        khachHang.setName(name);
        khachHang.setPhoneNumber(phoneNumber);
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
        return khachHangRepository.findByPhoneNumber(phone).map(this::authCustomerMap).orElseGet(Map::of);
    }

    @PostMapping("/auth/customers")
    public Map<String, Object> createAuthCustomer(@RequestParam String name, @RequestParam String email, @RequestParam String phoneNumber, @RequestParam String password) {
        Customer khachHang = new Customer();
        khachHang.setName(name);
        khachHang.setEmail(email);
        khachHang.setPhoneNumber(phoneNumber);
        khachHang.setPassword(password);
        khachHang.setStatus(EntityStatus.ACTIVE);
        return authCustomerMap(khachHangRepository.save(khachHang));
    }

    @PostMapping("/auth/customers/password")
    public void updateCustomerPassword(@RequestParam String email, @RequestParam String password) {
        khachHangRepository.findByEmail(email).ifPresent(kh -> {
            kh.setPassword(password);
            khachHangRepository.save(kh);
        });
    }

    @GetMapping("/auth/staff/by-email")
    public Map<String, Object> getAuthStaffByEmail(@RequestParam String email, @RequestParam(defaultValue = "false") boolean activeOnly) {
        return (activeOnly ? nhanVienRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE) : nhanVienRepository.findByEmail(email))
                .map(this::authStaffMap)
                .orElseGet(Map::of);
    }

    private Map<String, Object> customerMap(Customer kh) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", kh.getId());
        row.put("code", kh.getCode());
        row.put("name", kh.getName());
        row.put("email", kh.getEmail());
        row.put("phoneNumber", kh.getPhoneNumber());
        row.put("avatar", kh.getAvatar());
        row.put("address", kh.getAddress());
        row.put("province", kh.getProvince());
        row.put("district", kh.getDistrict());
        row.put("ward", kh.getWard());
        row.put("status", kh.getStatus() == null ? null : kh.getStatus().name());
        return row;
    }

    private Map<String, Object> authCustomerMap(Customer kh) {
        Map<String, Object> row = customerMap(kh);
        row.put("password", kh.getPassword());
        return row;
    }

    private Map<String, Object> authStaffMap(Staff nv) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", nv.getId());
        row.put("code", nv.getCode());
        row.put("name", nv.getName());
        row.put("email", nv.getEmail());
        row.put("avatar", nv.getAvatar());
        row.put("password", nv.getPassword());
        row.put("role", nv.getRole() == null ? null : nv.getRole().name());
        row.put("status", nv.getStatus() == null ? null : nv.getStatus().name());
        return row;
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }
}
