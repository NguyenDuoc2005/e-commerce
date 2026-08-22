package com.ecommerce.user.config;

import com.ecommerce.user.constant.EntityRole;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.constant.EntityVaiTro;
import com.ecommerce.user.entity.Staff;
import com.ecommerce.user.repository.StaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class LocalAdminSeeder {

    public static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

    @Bean
    CommandLineRunner seedLocalAdmin(StaffRepository nhanVienRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Staff admin = nhanVienRepository.findById(ADMIN_ID).orElseGet(Staff::new);
            admin.setId(ADMIN_ID);
            admin.setStatus(EntityStatus.ACTIVE);
            admin.setCode("ADMIN001");
            admin.setName("Local Admin");
            admin.setEmail("admin@ecommerce.local");
            admin.setPhoneNumber("0900000000");
            admin.setIdentityNumber("000000000001");
            admin.setAddress("Local development");
            admin.setRoleType(EntityVaiTro.QUAN_LY);
            admin.setRole(EntityRole.ADMIN);
            if (admin.getPassword() == null || admin.getPassword().isBlank()
                    || !passwordEncoder.matches("Admin@123", admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode("Admin@123"));
            }

            nhanVienRepository.save(admin);
        };
    }
}
