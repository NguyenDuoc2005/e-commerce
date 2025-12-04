package com.be.server.infrastructure.listener;

import com.be.server.entity.KhachHang;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateKhachHangEntityListener {

    @PrePersist
    private void onCreate(KhachHang nhanVien) {
        nhanVien.setMa(Helper.generateCodeKhachHang());
    }

}
