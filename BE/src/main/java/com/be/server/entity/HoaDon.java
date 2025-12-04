package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityPhuongThucThanhToan;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.infrastructure.listener.CreateHoaDonEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "hoa_don")
@DynamicUpdate
@EntityListeners(CreateHoaDonEntityListener.class)
public class HoaDon extends PrimaryEntity implements Serializable {

    @Column(name = "ma_hoa_don")
    private String ma;

    @Column(name = "ten_hoa_don")
    private String ten;

    @Column(name = "so_dien_thoai_khach_hang")
    private String sdt;

    @Column(name = "email")
    private String email;

    @Column(name = "ten_khach_hang")
    private String tenKH;

    @Column(name = "phi_van_chuyen")
    private Double phiVanChuyen;

    @Column(name = "dia_chi_giao_hang")
    private String diaChi;

    @Column(name = "tong_tien_sau_giam")
    private Double tongTienSauGiam;

    @Column(name = "tong_tien")
    private Double tongTien;

    @Column(name = "giam_gia")
    private Double giamGia;

    @Column(name = "du_no")
    private Double duNo;

    @Column(name = "hoan_phi")
    private Double hoanPhi;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "phuong_thuc_thanh_toan")
    private EntityPhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "loai_hoa_don")
    private EntityLoaiHoaDon loaiHoaDon;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang",referencedColumnName = "id")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_voucher",referencedColumnName = "id")
    private PhieuGiamGia voucher;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien",referencedColumnName = "id")
    private NhanVien nhanVien;

    @Column(name = "trang_thai_hoa_don")
    private EntityTrangThaiHoaDon trangThaiHoaDon;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LichSuTrangThaiHoaDon> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> hoaDonChiTiets = new ArrayList<>();

}
