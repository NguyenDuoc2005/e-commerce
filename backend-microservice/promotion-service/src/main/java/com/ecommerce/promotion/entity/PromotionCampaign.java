package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.constant.CampaignType;
import com.ecommerce.promotion.constant.StatusPromotion;
import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "promotion_campaign")
@DynamicUpdate
public class PromotionCampaign extends PrimaryEntity {

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "discount_value")
    private Double discountValue;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "end_date")
    private Long endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_status")
    private StatusPromotion trangThai;

    @Column(name = "seller_id", length = 36)
    private String sellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 32)
    private CampaignType campaignType = CampaignType.STANDARD;

    @Column(name = "registration_start_date")
    private Long registrationStartDate;

    @Column(name = "registration_end_date")
    private Long registrationEndDate;

    @Column(name = "created_by_staff_id", length = 36)
    private String createdByStaffId;

    @PrePersist
    void defaultCampaignType() {
        if (campaignType == null) campaignType = CampaignType.STANDARD;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
    public StatusPromotion getTrangThai() { return trangThai; }
    public void setTrangThai(StatusPromotion trangThai) { this.trangThai = trangThai; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public CampaignType getCampaignType() { return campaignType; }
    public void setCampaignType(CampaignType campaignType) { this.campaignType = campaignType; }
    public Long getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(Long registrationStartDate) { this.registrationStartDate = registrationStartDate; }
    public Long getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(Long registrationEndDate) { this.registrationEndDate = registrationEndDate; }
    public String getCreatedByStaffId() { return createdByStaffId; }
    public void setCreatedByStaffId(String createdByStaffId) { this.createdByStaffId = createdByStaffId; }
}
