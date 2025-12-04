package com.be.server.core.admin.hoadon.model.response;

import java.time.LocalDateTime;

public interface TrangThaiThoiGianResponse {

    String getTrangThai();
    LocalDateTime getThoiGian();
    String getNote();
}