package com.ecommerce.auth.config;

import com.ecommerce.auth.constant.EntityRole;
import com.ecommerce.auth.constant.EntityStatus;
import com.ecommerce.auth.constant.EntityVaiTro;
import com.ecommerce.auth.entity.NhanVien;
import com.ecommerce.auth.repository.NhanVienAuthRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class LocalAdminSeeder {

    public static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";
    public static final String ADMIN_EMAIL = "admin@ecommerce.local";
    public static final String ADMIN_PASSWORD = "Admin@123";

    @Bean
    CommandLineRunner seedLocalAdmin(
            NhanVienAuthRepository nhanVienAuthRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (nhanVienAuthRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
                return;
            }

            NhanVien admin = new NhanVien();
            admin.setId(ADMIN_ID);
            admin.setStatus(EntityStatus.ACTIVE);
            admin.setMa("ADMIN001");
            admin.setTen("Local Admin");
            admin.setEmail(ADMIN_EMAIL);
            admin.setSdt("0900000000");
            admin.setCccd("000000000001");
            admin.setDiaChi("Local development");
            admin.setVaitro(EntityVaiTro.QUAN_LY);
            admin.setChucVu(EntityRole.ADMIN);
            admin.setMatKhau(passwordEncoder.encode(ADMIN_PASSWORD));

            nhanVienAuthRepository.save(admin);
        };
    }
}
