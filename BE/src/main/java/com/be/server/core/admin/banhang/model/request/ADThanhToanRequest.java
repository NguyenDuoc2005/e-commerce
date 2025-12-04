package com.be.server.core.admin.banhang.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADThanhToanRequest {

    private String ten;

    private String sdt;

    private String diaChi;

    private String idNV;

    private String idHD;

    private String idPGG;

    private Double tienHang;

    private Double giamGia;

    private Double tongTien;

    private Integer check;

    private Double tienShip;

    private String phuongThucThanhToan;
}
