package com.be.server.core.admin.hoadon.model.request;

import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ThanhToanRequest {

    private String hoaDonId;
    private Double soTienKhachDua;
    private Double soTienTraLai;
    private Double soTienGoc;
    private String ghiChu;
    private String loaiGiaoDich;
    private String nhanVienId;
    private EntityTrangThaiHoaDon trangThai;
}
