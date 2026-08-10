package com.ecommerce.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {

    @GetMapping("/auth/customers/by-email")
    Map<String, Object> getCustomerByEmail(@RequestParam("email") String email, @RequestParam("activeOnly") boolean activeOnly);

    @GetMapping("/auth/customers/by-phone")
    Map<String, Object> getCustomerByPhone(@RequestParam("phone") String phone);

    @PostMapping("/auth/customers")
    Map<String, Object> createCustomer(
            @RequestParam("ten") String ten,
            @RequestParam("email") String email,
            @RequestParam("sdt") String sdt,
            @RequestParam("matKhau") String matKhau
    );

    @PostMapping("/auth/customers/password")
    void updateCustomerPassword(@RequestParam("email") String email, @RequestParam("matKhau") String matKhau);

    @GetMapping("/auth/staff/by-email")
    Map<String, Object> getStaffByEmail(@RequestParam("email") String email, @RequestParam("activeOnly") boolean activeOnly);
}
