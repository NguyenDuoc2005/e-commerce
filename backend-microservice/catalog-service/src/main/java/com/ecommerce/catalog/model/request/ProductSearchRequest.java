package com.ecommerce.catalog.model.request;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.common.base.PageableRequest;

public class ProductSearchRequest extends PageableRequest {
    private String q;
    private String idSP;
    private String categoryId;
    private String materialId;
    private String brandId;
    private String soleTypeId;
    private Double giaMin;
    private Double giaMax;
    private String brandIds;
    private String materialIds;
    private String soleTypeIds;
    private String categoryIds;
    private String thuongHieuIds;
    private String chatLieuIds;
    private String loaiDeIds;
    private String danhMucIds;
    private String status;
    private String sellerId;
    private String sellerSlug;
    private Double ratingMin;
    private String attributeFilters;
    private EntityStatus entityStatus;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getSoleTypeId() { return soleTypeId; }
    public void setSoleTypeId(String soleTypeId) { this.soleTypeId = soleTypeId; }
    public Double getGiaMin() { return giaMin; }
    public void setGiaMin(Double giaMin) { this.giaMin = giaMin; }
    public Double getGiaMax() { return giaMax; }
    public void setGiaMax(Double giaMax) { this.giaMax = giaMax; }
    public String getBrandIds() { return brandIds; }
    public void setBrandIds(String brandIds) { this.brandIds = brandIds; }
    public String getMaterialIds() { return materialIds; }
    public void setMaterialIds(String materialIds) { this.materialIds = materialIds; }
    public String getSoleTypeIds() { return soleTypeIds; }
    public void setSoleTypeIds(String soleTypeIds) { this.soleTypeIds = soleTypeIds; }
    public String getCategoryIds() { return categoryIds; }
    public void setCategoryIds(String categoryIds) { this.categoryIds = categoryIds; }
    public String getThuongHieuIds() { return thuongHieuIds; }
    public void setThuongHieuIds(String thuongHieuIds) { this.thuongHieuIds = thuongHieuIds; }
    public String getChatLieuIds() { return chatLieuIds; }
    public void setChatLieuIds(String chatLieuIds) { this.chatLieuIds = chatLieuIds; }
    public String getLoaiDeIds() { return loaiDeIds; }
    public void setLoaiDeIds(String loaiDeIds) { this.loaiDeIds = loaiDeIds; }
    public String getDanhMucIds() { return danhMucIds; }
    public void setDanhMucIds(String danhMucIds) { this.danhMucIds = danhMucIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getSellerSlug() { return sellerSlug; }
    public void setSellerSlug(String sellerSlug) { this.sellerSlug = sellerSlug; }
    public Double getRatingMin() { return ratingMin; }
    public void setRatingMin(Double ratingMin) { this.ratingMin = ratingMin; }
    public String getAttributeFilters() { return attributeFilters; }
    public void setAttributeFilters(String attributeFilters) { this.attributeFilters = attributeFilters; }
    public EntityStatus getEntityStatus() { return entityStatus; }
    public void setEntityStatus(EntityStatus entityStatus) { this.entityStatus = entityStatus; }
}
