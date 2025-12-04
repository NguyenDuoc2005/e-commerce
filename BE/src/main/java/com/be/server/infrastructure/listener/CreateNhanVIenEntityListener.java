package com.be.server.infrastructure.listener;

import com.be.server.entity.NhanVien;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateNhanVIenEntityListener {

    @PrePersist
    private void onCreate(NhanVien nhanVien) {
        nhanVien.setMa(Helper.generateCodeNhanVien());
    }

}
