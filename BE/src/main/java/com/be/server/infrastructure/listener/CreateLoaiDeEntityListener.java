package com.be.server.infrastructure.listener;

import com.be.server.entity.LoaiDe;
import com.be.server.entity.SanPham;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateLoaiDeEntityListener {

    @PrePersist
    private void onCreate(LoaiDe sanPham) {
        sanPham.setMa(Helper.generateCodeLoaiDe());
    }

}
