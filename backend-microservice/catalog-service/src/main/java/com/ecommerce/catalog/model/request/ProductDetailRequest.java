package com.ecommerce.catalog.model.request;

import org.springframework.web.multipart.MultipartFile;

public class ProductDetailRequest {
    private String id;
    private Double giaBan;
    private String ten;
    private String ma;
    private String moTa;
    private MultipartFile anh;
    private Integer soLuong;
    private String idThuongHieu;
    private String idXuatXu;
    private String idLoaiDe;
    private String idDanhMuc;
    private String idChatLieu;
    private String idMau;
    private String idSize;
    private String idSP;
    private String check;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Double getGiaBan() { return giaBan; }
    public void setGiaBan(Double giaBan) { this.giaBan = giaBan; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public MultipartFile getAnh() { return anh; }
    public void setAnh(MultipartFile anh) { this.anh = anh; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public String getIdThuongHieu() { return idThuongHieu; }
    public void setIdThuongHieu(String idThuongHieu) { this.idThuongHieu = idThuongHieu; }
    public String getIdXuatXu() { return idXuatXu; }
    public void setIdXuatXu(String idXuatXu) { this.idXuatXu = idXuatXu; }
    public String getIdLoaiDe() { return idLoaiDe; }
    public void setIdLoaiDe(String idLoaiDe) { this.idLoaiDe = idLoaiDe; }
    public String getIdDanhMuc() { return idDanhMuc; }
    public void setIdDanhMuc(String idDanhMuc) { this.idDanhMuc = idDanhMuc; }
    public String getIdChatLieu() { return idChatLieu; }
    public void setIdChatLieu(String idChatLieu) { this.idChatLieu = idChatLieu; }
    public String getIdMau() { return idMau; }
    public void setIdMau(String idMau) { this.idMau = idMau; }
    public String getIdSize() { return idSize; }
    public void setIdSize(String idSize) { this.idSize = idSize; }
    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public String getCheck() { return check; }
    public void setCheck(String check) { this.check = check; }
}
