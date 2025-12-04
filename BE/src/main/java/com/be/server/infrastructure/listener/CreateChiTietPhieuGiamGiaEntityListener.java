package com.be.server.infrastructure.listener;

import com.be.server.entity.KhachHang;
import com.be.server.entity.PhieuGiamGiaChiTiet;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateChiTietPhieuGiamGiaEntityListener {

    @PrePersist
    private void onCreate(PhieuGiamGiaChiTiet nhanVien) {
        nhanVien.setMa(Helper.generateCodeVoucher());
    }

}
