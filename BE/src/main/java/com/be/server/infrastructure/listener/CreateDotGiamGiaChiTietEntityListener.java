package com.be.server.infrastructure.listener;

import com.be.server.entity.DotGiamGia;
import com.be.server.entity.DotGiamGiaChiTietSanPham;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateDotGiamGiaChiTietEntityListener {

    @PrePersist
    private void onCreate(DotGiamGiaChiTietSanPham dotGiamGiaCT) {
        dotGiamGiaCT.setMa(Helper.generateCodeDotGiamGiaCT());

    }

}