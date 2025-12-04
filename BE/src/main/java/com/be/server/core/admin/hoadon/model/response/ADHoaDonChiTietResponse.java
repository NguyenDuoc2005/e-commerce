package com.be.server.core.admin.hoadon.model.response;

import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ADHoaDonChiTietResponse {
    private String maHoaDonChiTiet;
    private String tenSanPham;
    private String anhSanPham;
    private String thuongHieu;
    private String xuatSu;
    private String mauSac;
    private String size;
    private Integer soLuong;
    private Double giaBan;
    private Double thanhTien;
}