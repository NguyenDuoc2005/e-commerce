package com.be.server.utils;

import com.be.server.entity.NhanVien;
import com.be.server.infrastructure.constant.EntityRole;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.NhanVienRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataInitializer {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @PostConstruct
    public void init() {
        if (appConfig.isGenerateDb()) {
            // Kiểm tra xem nhân viên đã tồn tại chưa
            if (!nhanVienRepository.existsById(appConfig.getUserCode())) {
                NhanVien employee = new NhanVien();
                employee.setMa(appConfig.getUserCode());
                employee.setTen(appConfig.getUserName());
                employee.setEmail(appConfig.getUserEmail());
                employee.setSdt(appConfig.getUserPhone());
                employee.setStatus(EntityStatus.ACTIVE);
                employee.setChucVu(appConfig.getRole() == "ADMIN" ? EntityRole.ADMIN : EntityRole.STAFF);
                employee.setMatKhau("$2a$12$Im2Kx70E9o9a.JUkANbTe.4Tqa3imV9/WJO4LOEt7qBlgmF.Qp5B6");
                nhanVienRepository.save(employee);
                System.out.println("Employee data saved to database: " + employee);
            } else {
                System.out.println("Employee with code " + appConfig.getUserCode() + " already exists.");
            }
        }
    }
}
