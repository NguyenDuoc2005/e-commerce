package com.ecommerce.catalog.model.request;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.common.base.PageableRequest;

public class ProductSearchRequest extends PageableRequest {
    private String q;
    private String idSP;
    private String danhMucId;
    private String chatLieuId;
    private String thuongHieuId;
    private String loaiDeId;
    private Double giaMin;
    private Double giaMax;
    private String thuongHieuIds;
    private String chatLieuIds;
    private String loaiDeIds;
    private String danhMucIds;
    private String status;
    private EntityStatus entityStatus;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public String getDanhMucId() { return danhMucId; }
    public void setDanhMucId(String danhMucId) { this.danhMucId = danhMucId; }
    public String getChatLieuId() { return chatLieuId; }
    public void setChatLieuId(String chatLieuId) { this.chatLieuId = chatLieuId; }
    public String getThuongHieuId() { return thuongHieuId; }
    public void setThuongHieuId(String thuongHieuId) { this.thuongHieuId = thuongHieuId; }
    public String getLoaiDeId() { return loaiDeId; }
    public void setLoaiDeId(String loaiDeId) { this.loaiDeId = loaiDeId; }
    public Double getGiaMin() { return giaMin; }
    public void setGiaMin(Double giaMin) { this.giaMin = giaMin; }
    public Double getGiaMax() { return giaMax; }
    public void setGiaMax(Double giaMax) { this.giaMax = giaMax; }
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
    public EntityStatus getEntityStatus() { return entityStatus; }
    public void setEntityStatus(EntityStatus entityStatus) { this.entityStatus = entityStatus; }
}
