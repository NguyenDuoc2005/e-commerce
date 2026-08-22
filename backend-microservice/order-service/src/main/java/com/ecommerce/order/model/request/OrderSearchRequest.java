package com.ecommerce.order.model.request;

import com.ecommerce.common.base.PageableRequest;
import com.ecommerce.order.constant.OrderStatusConstant;

public class OrderSearchRequest extends PageableRequest {
    private String q;
    private String idSP;
    private Long startDate;
    private Long endDate;
    private OrderStatusConstant status;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getIdSP() { return idSP; }
    public void setIdSP(String idSP) { this.idSP = idSP; }
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
    public OrderStatusConstant getStatus() { return status; }
    public void setStatus(OrderStatusConstant status) { this.status = status; }
}
