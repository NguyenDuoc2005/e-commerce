package com.ecommerce.order.model.request;

import com.ecommerce.common.base.PageableRequest;

public class OrderDetailRequest extends PageableRequest {
    private String maOrder;

    public String getMaOrder() { return maOrder; }
    public void setMaOrder(String maOrder) { this.maOrder = maOrder; }
}
