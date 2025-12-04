package com.be.server.infrastructure.listener;

import com.be.server.entity.SanPham;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateSanPhamChiTietEntityListener {

    @PrePersist
    private void onCreate(SanPhamChiTiet sanPham) {
        sanPham.setMa(Helper.generateCodeSanPhamChiTiet());
    }

}
