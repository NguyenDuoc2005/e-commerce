package com.ecommerce.promotion.model.request;

import java.util.List;

public class UpdatePromotionRequest {
    private String id;
    private String name;
    private Double value;
    private Long startDate;
    private Long endDate;
    private List<IdProductDetail> idProductDetails;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
