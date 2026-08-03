package com.ecommerce.order.model.request;

import com.ecommerce.common.base.PageableRequest;

public class SanPhamChiTietSearchRequest extends PageableRequest {
    private String idSP;
    private Integer entityStatus;
    private Double priceMin;
    private Double priceMax;
    private String idKT;
    private String idMS;

    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public Integer getEntityStatus() { return entityStatus; }
    public void setEntityStatus(Integer entityStatus) { this.entityStatus = entityStatus; }
    public Double getPriceMin() { return priceMin; }
    public void setPriceMin(Double priceMin) { this.priceMin = priceMin; }
    public Double getPriceMax() { return priceMax; }
    public void setPriceMax(Double priceMax) { this.priceMax = priceMax; }
    public String getIdKT() { return idKT; }
    public void setIdKT(String idKT) { this.idKT = idKT; }
    public String getIdMS() { return idMS; }
    public void setIdMS(String idMS) { this.idMS = idMS; }
}
