package com.ecommerce.user.entity;

import com.ecommerce.user.constant.EntityRole;
import com.ecommerce.user.constant.EntityVaiTro;
import com.ecommerce.user.entity.base.PrimaryEntity;
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

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getTinh() { return tinh; }
    public void setTinh(String tinh) { this.tinh = tinh; }
    public String getHuyen() { return huyen; }
    public void setHuyen(String huyen) { this.huyen = huyen; }
    public String getXa() { return xa; }
    public void setXa(String xa) { this.xa = xa; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public EntityVaiTro getVaitro() { return vaitro; }
    public void setVaitro(EntityVaiTro vaitro) { this.vaitro = vaitro; }
    public Boolean getGioiTimh() { return gioiTimh; }
    public void setGioiTimh(Boolean gioiTimh) { this.gioiTimh = gioiTimh; }
    public EntityRole getChucVu() { return chucVu; }
    public void setChucVu(EntityRole chucVu) { this.chucVu = chucVu; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
}
