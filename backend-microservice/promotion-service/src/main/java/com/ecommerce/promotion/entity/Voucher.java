package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

@Entity
@Table(name = "voucher")
@DynamicUpdate
public class Voucher extends PrimaryEntity implements Serializable {

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "discount_value")
    private Double discountValue;

    @Transient
    private Double actualDiscountValue;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "condition_amount")
    private Double conditionAmount;

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Column(name = "discount_type")
    private Boolean discountType;

    @Column(name = "discount_method")
    private Boolean discountMethod;

    @Column(name = "seller_id", length = 36)
    private String sellerId;

    @PrePersist
    void generateCode() {
        if (code == null || code.isBlank()) {
            code = String.format("PGG%04d", new Random().nextInt(10000));
        }
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
    public Double getActualDiscountValue() { return actualDiscountValue; }
    public void setActualDiscountValue(Double actualDiscountValue) { this.actualDiscountValue = actualDiscountValue; }
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
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
}
