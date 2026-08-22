package com.ecommerce.catalog.model.request;

public class ProductRequest {
    private String stt;
    private String id;
    private String name;
    private String code;
    private String description;
    private String idBrand;
    private String idXuatXu;
    private String idSoleType;
    private String idCategory;
    private String idMaterial;
    private String idSP;
    private String sellerId;
    private String attributes;
    private Integer tongTien;

    public String getStt() { return stt; }
    public void setStt(String stt) { this.stt = stt; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIdBrand() { return idBrand; }
    public void setIdBrand(String idBrand) { this.idBrand = idBrand; }
    public String getIdXuatXu() { return idXuatXu; }
    public void setIdXuatXu(String idXuatXu) { this.idXuatXu = idXuatXu; }
    public String getIdSoleType() { return idSoleType; }
    public void setIdSoleType(String idSoleType) { this.idSoleType = idSoleType; }
    public String getIdCategory() { return idCategory; }
    public void setIdCategory(String idCategory) { this.idCategory = idCategory; }
    public String getIdMaterial() { return idMaterial; }
    public void setIdMaterial(String idMaterial) { this.idMaterial = idMaterial; }
    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }
    public Integer getTongTien() { return tongTien; }
    public void setTongTien(Integer tongTien) { this.tongTien = tongTien; }
}
