package com.be.server.core.permitall.thanhtoan.model.request;

import com.be.server.core.permitall.thanhtoan.model.Respones.ListSP;
import com.be.server.entity.SanPhamChiTiet;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PMThanhToan {

    private String hoTen;

    private String soDienThoai;

    private String diaChi;

    private String ghiChu;

    private String email;

    private String maGiamGia;

    private String hinhThucThanhToan;

    private Double tongTien;

    private Double phiShip;

    private Double giamGia;

    private Double tongCong;

    private List<ListSP> sanPham;

    @JsonProperty("KhachHang")
    private String KhachHang;
}