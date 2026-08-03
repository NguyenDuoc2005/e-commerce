package com.ecommerce.order.model.response;

import com.ecommerce.order.constant.EntityLoaiHoaDon;
import com.ecommerce.order.constant.EntityTrangThaiHoaDon;

public class HoaDonResponse {
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

    public HoaDonResponse(String id, String maHoaDon, String tenKhachHang, String sdtKhachHang, String maNhanVien, String tenNhanVien, Double tongTien, Integer loaiHoaDon, Long createdDate, Integer status) {
        this.id = id;
        this.maHoaDon = maHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.sdtKhachHang = sdtKhachHang;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.tongTien = tongTien;
        this.loaiHoaDon = loaiHoaDon == null ? null : EntityLoaiHoaDon.values()[loaiHoaDon];
        this.createdDate = createdDate;
        this.status = status == null ? null : EntityTrangThaiHoaDon.values()[status];
    }

    public String getId() { return id; }
    public String getMaHoaDon() { return maHoaDon; }
    public String getTenKhachHang() { return tenKhachHang; }
    public String getSdtKhachHang() { return sdtKhachHang; }
    public String getMaNhanVien() { return maNhanVien; }
    public String getTenNhanVien() { return tenNhanVien; }
    public Double getTongTien() { return tongTien; }
    public EntityLoaiHoaDon getLoaiHoaDon() { return loaiHoaDon; }
    public Long getCreatedDate() { return createdDate; }
    public EntityTrangThaiHoaDon getStatus() { return status; }
}
