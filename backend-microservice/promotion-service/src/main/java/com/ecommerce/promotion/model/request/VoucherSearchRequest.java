package com.ecommerce.promotion.model.request;

import com.ecommerce.common.base.PageableRequest;
import com.ecommerce.promotion.constant.EntityStatus;

import java.time.LocalDate;

public class VoucherSearchRequest extends PageableRequest {
    private String q;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer kieuGiam;
    private Integer status;
    private Boolean kieu;
    private EntityStatus entityStatus;
    private Boolean trangThai;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getKieuGiam() { return kieuGiam; }
    public void setKieuGiam(Integer kieuGiam) { this.kieuGiam = kieuGiam; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Boolean getKieu() { return kieu; }
    public void setKieu(Boolean kieu) { this.kieu = kieu; }
    public EntityStatus getEntityStatus() { return entityStatus; }
    public void setEntityStatus(EntityStatus entityStatus) { this.entityStatus = entityStatus; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}
