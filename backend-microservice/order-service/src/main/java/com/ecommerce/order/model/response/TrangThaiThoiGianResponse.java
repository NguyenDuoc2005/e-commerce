package com.ecommerce.order.model.response;

import java.time.LocalDateTime;

public interface TrangThaiThoiGianResponse {
    String getTrangThai();
    LocalDateTime getThoiGian();
    String getNote();
}
