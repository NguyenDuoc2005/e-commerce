package com.be.server.core.admin.thongke.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThongKeDoanhThuResponse {

    private Double doanhSoThangNay;
    private Integer soHoaDonThangNay;
    private Double doanhSoHomNay;
    private Integer soHoaDonHomNay;
    private Integer hangBanDuocThangNay;

}
