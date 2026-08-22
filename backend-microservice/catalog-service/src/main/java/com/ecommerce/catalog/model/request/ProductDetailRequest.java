package com.ecommerce.catalog.model.request;

import org.springframework.web.multipart.MultipartFile;

public class ProductDetailRequest {
    private String id;
    private Double salePrice;
    private String name;
    private String code;
    private String description;
    private MultipartFile imageUrl;
    private Integer quantity;
    private String idBrand;
    private String idXuatXu;
    private String idSoleType;
    private String idCategory;
    private String idMaterial;
    private String idMau;
    private String idSize;
    private String idSP;
    private String sellerId;
    private String check;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public MultipartFile getImageUrl() { return imageUrl; }
    public void setImageUrl(MultipartFile imageUrl) { this.imageUrl = imageUrl; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
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
    public String getIdMau() { return idMau; }
    public void setIdMau(String idMau) { this.idMau = idMau; }
    public String getIdSize() { return idSize; }
    public void setIdSize(String idSize) { this.idSize = idSize; }
    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getCheck() { return check; }
    public void setCheck(String check) { this.check = check; }
}
