package com.ecommerce.order.model.response;

import java.time.LocalDateTime;

public interface PaymentHistoryResponse {
    Integer getStt();
    Double getSoTien();
    LocalDateTime getThoiGian();
    String getMaGiaoDich();
    String getLoaiGiaoDich();
    String getGhiChu();
    String getTenStaff();
    String getOrderId();
}
