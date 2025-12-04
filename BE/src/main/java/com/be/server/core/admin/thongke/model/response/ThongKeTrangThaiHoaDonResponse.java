package com.be.server.core.admin.thongke.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThongKeTrangThaiHoaDonResponse {
    private String trangThai;
    private long soLuong;
    private double tiLePhanTram;
}
