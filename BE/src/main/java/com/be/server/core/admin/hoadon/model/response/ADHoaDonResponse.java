package com.be.server.core.admin.hoadon.model.response;

import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ADHoaDonResponse {
    private String id;
    private String maHoaDon;
    private String tenKhachHang;
    private String sdtKhachHang;
    private String maNhanVien;
    private String tenNhanVien;
    private Double tongTien;
    private EntityLoaiHoaDon loaiHoaDon;
    private Long createdDate;
    private EntityTrangThaiHoaDon status;
}