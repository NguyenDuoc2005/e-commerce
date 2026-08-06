package com.ecommerce.user.config;

import com.ecommerce.user.constant.EntityRole;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.constant.EntityVaiTro;
import com.ecommerce.user.entity.NhanVien;
import com.ecommerce.user.repository.NhanVienRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalAdminSeeder {

    public static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

    @Bean
    CommandLineRunner seedLocalAdmin(NhanVienRepository nhanVienRepository) {
        return args -> {
            if (nhanVienRepository.existsById(ADMIN_ID)) {
                return;
            }

            NhanVien admin = new NhanVien();
            admin.setId(ADMIN_ID);
            admin.setStatus(EntityStatus.ACTIVE);
            admin.setMa("ADMIN001");
            admin.setTen("Local Admin");
            admin.setEmail("admin@ecommerce.local");
            admin.setSdt("0900000000");
            admin.setCccd("000000000001");
            admin.setDiaChi("Local development");
            admin.setVaitro(EntityVaiTro.QUAN_LY);
            admin.setChucVu(EntityRole.ADMIN);

            nhanVienRepository.save(admin);
        };
    }
}
