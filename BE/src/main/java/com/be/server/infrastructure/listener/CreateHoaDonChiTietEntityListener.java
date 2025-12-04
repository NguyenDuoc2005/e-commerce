package com.be.server.infrastructure.listener;

import com.be.server.entity.HoaDon;
import com.be.server.entity.HoaDonChiTiet;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateHoaDonChiTietEntityListener {

    @PrePersist
    private void onCreate(HoaDonChiTiet HoaDon) {
        HoaDon.setMa(Helper.generateCodeHoaDonCT());
    }

}
