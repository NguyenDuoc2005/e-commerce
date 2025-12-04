package com.be.server.core.admin.hoadon.model.response;

import java.time.LocalDateTime;

public interface LichSuThanhToanResponse {

    Integer getStt();
    Double getSoTien();
    LocalDateTime getThoiGian();
    String getMaGiaoDich();
    String getLoaiGiaoDich();
    String getGhiChu();
    String getTenNhanVien();
    String getHoaDonId();
}