package com.be.server.core.admin.hoadon.model.response;

import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ADHoaDonResponse1 {

    private String id;
    private String maHoaDon;
    private String anh;
    private String tenSanPham;
    private String tenThuongHieu;
    private String mauSac;
    private String kichCo;
    private Integer soLuong;
    private Double giaBan;
    private EntityTrangThaiHoaDon status;
    private Double tongTien;
}