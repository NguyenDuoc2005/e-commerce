package com.be.server.infrastructure.listener;

import com.be.server.entity.SanPham;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateSanPhamEntityListener {

    @PrePersist
    private void onCreate(SanPham sanPham) {
        sanPham.setMa(Helper.generateCodeSanPham());
    }

}
