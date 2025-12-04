package com.be.server.core.admin.thongke.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopSanPhamBanChayResponse {

    private String id;
    private String maSanPham;
    private String tenSanPham;
    private String anhSanPham;
    private Long soLuongBan;
    private Double doanhThu;
    private String thuongHieu;
    private Double giaBan;
}
