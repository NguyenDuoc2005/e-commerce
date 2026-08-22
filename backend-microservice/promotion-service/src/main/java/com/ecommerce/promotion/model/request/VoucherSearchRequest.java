package com.ecommerce.promotion.model.request;

import com.ecommerce.common.base.PageableRequest;
import com.ecommerce.promotion.constant.EntityStatus;

import java.time.LocalDate;

public class VoucherSearchRequest extends PageableRequest {
    private String q;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer discountMethod;
    private Integer status;
    private Boolean kieu;
    private EntityStatus entityStatus;
    private Boolean trangThai;
    private String sellerId;
    private Boolean platformOnly;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getDiscountMethod() { return discountMethod; }
    public void setDiscountMethod(Integer discountMethod) { this.discountMethod = discountMethod; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Boolean getKieu() { return kieu; }
    public void setKieu(Boolean kieu) { this.kieu = kieu; }
    public EntityStatus getEntityStatus() { return entityStatus; }
    public void setEntityStatus(EntityStatus entityStatus) { this.entityStatus = entityStatus; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Boolean getPlatformOnly() { return platformOnly; }
    public void setPlatformOnly(Boolean platformOnly) { this.platformOnly = platformOnly; }
}
