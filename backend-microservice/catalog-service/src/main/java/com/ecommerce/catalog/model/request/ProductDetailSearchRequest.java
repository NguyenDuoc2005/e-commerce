package com.ecommerce.catalog.model.request;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.common.base.PageableRequest;

public class ProductDetailSearchRequest extends PageableRequest {
    private String q;
    private String idSP;
    private String idSanPham;
    private String status;
    private String priceMin;
    private String priceMax;
    private String idMS;
    private String idKT;
    private EntityStatus entityStatus;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public String getIdSanPham() { return idSanPham; }
    public void setIdSanPham(String idSanPham) { this.idSanPham = idSanPham; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriceMin() { return priceMin; }
    public void setPriceMin(String priceMin) { this.priceMin = priceMin; }
    public String getPriceMax() { return priceMax; }
    public void setPriceMax(String priceMax) { this.priceMax = priceMax; }
    public String getIdMS() { return idMS; }
    public void setIdMS(String idMS) { this.idMS = idMS; }
    public String getIdKT() { return idKT; }
    public void setIdKT(String idKT) { this.idKT = idKT; }
    public EntityStatus getEntityStatus() { return entityStatus; }
    public void setEntityStatus(EntityStatus entityStatus) { this.entityStatus = entityStatus; }
}
