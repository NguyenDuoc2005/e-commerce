package com.ecommerce.user.model.request;

import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

public class UserUpsertRequest {

    private String id;
    private String code;
    private String ten;
    private String email;
    private String sdt;
    private String diaChi;
    private String xa;
    private String huyen;
    private String tinh;
    private String cccd;
    private Date ngaySinh;
    private Boolean gioiTinh;
    private MultipartFile avatar;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getXa() { return xa; }
    public void setXa(String xa) { this.xa = xa; }
    public String getHuyen() { return huyen; }
    public void setHuyen(String huyen) { this.huyen = huyen; }
    public String getTinh() { return tinh; }
    public void setTinh(String tinh) { this.tinh = tinh; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }
    public Boolean getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(Boolean gioiTinh) { this.gioiTinh = gioiTinh; }
    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar; }
}
