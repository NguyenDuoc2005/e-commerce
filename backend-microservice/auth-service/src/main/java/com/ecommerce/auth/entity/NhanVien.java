package com.ecommerce.auth.entity;

import com.ecommerce.auth.constant.EntityRole;
import com.ecommerce.auth.constant.EntityVaiTro;
import com.ecommerce.auth.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nhan_vien")
@DynamicUpdate
public class NhanVien extends PrimaryEntity implements Serializable {

    @Column(name = "ma_nhan_vien")
    private String ma;

    @Column(name = "ten_nhan_vien")
    private String ten;

    @Column(name = "tinh")
    private String tinh;

    @Column(name = "huyen")
    private String huyen;

    @Column(name = "xa")
    private String xa;

    @Column(name = "so_dien_thoai")
    private String sdt;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "ngay_sinh")
    private Date ngaySinh;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "email")
    private String email;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "vai_tro")
    private EntityVaiTro vaitro;

    @Column(name = "gioi_timh")
    private Boolean gioiTimh;

    @Column(name = "chuc_vu")
    private EntityRole chucVu;

    @Column(name = "mat_khau")
    private String matKhau;

    public String getTen() {
        return ten;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getEmail() {
        return email;
    }

    public String getMatKhau() {
        return matKhau;
    }
}
