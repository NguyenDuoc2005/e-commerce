package com.be.server.infrastructure.listener;

import com.be.server.entity.PhieuGiamGia;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateVoucherEntityListener {

    @PrePersist
    private void onCreate(PhieuGiamGia sanPham) {
        sanPham.setMa(Helper.generateCodeVoucher());
    }


    
}
