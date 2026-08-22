package com.ecommerce.promotion.model.request;

import java.sql.Date;
import java.util.List;

public class VoucherRequest {
    private String id;
    private String code;
    private String name;
    private Double LoiPhanNay;
    private Integer quantity;
    private Date startDate;
    private Date endDate;
    private Double conditionAmount;
    private Double maxDiscountAmount;
    private Boolean discountType;
    private Boolean discountMethod;
    private List<String> khachHangIds;
    private String sellerId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLoiPhanNay() { return LoiPhanNay; }
    public void setLoiPhanNay(Double loiPhanNay) { LoiPhanNay = loiPhanNay; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Double getConditionAmount() { return conditionAmount; }
    public void setConditionAmount(Double conditionAmount) { this.conditionAmount = conditionAmount; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    public Boolean getDiscountType() { return discountType; }
    public void setDiscountType(Boolean discountType) { this.discountType = discountType; }
    public Boolean getDiscountMethod() { return discountMethod; }
    public void setDiscountMethod(Boolean discountMethod) { this.discountMethod = discountMethod; }
    public List<String> getCustomerIds() { return khachHangIds; }
    public void setCustomerIds(List<String> khachHangIds) { this.khachHangIds = khachHangIds; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
}
