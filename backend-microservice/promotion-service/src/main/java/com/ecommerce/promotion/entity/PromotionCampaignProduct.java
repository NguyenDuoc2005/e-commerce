package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.constant.Status;
import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "promotion_campaign_product")
@DynamicUpdate
public class PromotionCampaignProduct extends PrimaryEntity {

    @Column(name = "code")
    private String code;

    @Column(name = "price_before_discount")
    private Double priceBeforeDiscount;

    @Column(name = "price_after_discount")
    private Double priceAfterDiscount;

    @Enumerated(EnumType.STRING)
    @Column(name = "detail_status")
    private Status detailStatus;

    @Column(name = "product_variant_id")
    private String productVariantId;

    @ManyToOne
    @JoinColumn(name = "promotion_campaign_id", referencedColumnName = "id")
    private PromotionCampaign promotionCampaign;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getPriceBeforeDiscount() { return priceBeforeDiscount; }
    public void setPriceBeforeDiscount(Double priceBeforeDiscount) { this.priceBeforeDiscount = priceBeforeDiscount; }
    public Double getPriceAfterDiscount() { return priceAfterDiscount; }
    public void setPriceAfterDiscount(Double priceAfterDiscount) { this.priceAfterDiscount = priceAfterDiscount; }
    public Status getDetailStatus() { return detailStatus; }
    public void setDetailStatus(Status detailStatus) { this.detailStatus = detailStatus; }
    public Status getTrangThai() { return detailStatus; }
    public void setTrangThai(Status detailStatus) { this.detailStatus = detailStatus; }
    public String getProductVariantId() { return productVariantId; }
    public void setProductVariantId(String productVariantId) { this.productVariantId = productVariantId; }
    public PromotionCampaign getPromotionCampaign() { return promotionCampaign; }
    public void setPromotionCampaign(PromotionCampaign promotionCampaign) { this.promotionCampaign = promotionCampaign; }
}
