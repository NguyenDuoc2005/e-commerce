package com.ecommerce.promotion.model.request;

import java.util.ArrayList;
import java.util.List;

public class CreatePromotionRequest {
    private String name;
    private Double value;
    private Long startDate;
    private Long endDate;
    private List<IdProductDetail> idProductDetails = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
    public List<IdProductDetail> getIdProductDetails() { return idProductDetails; }
    public void setIdProductDetails(List<IdProductDetail> idProductDetails) { this.idProductDetails = idProductDetails; }
}
