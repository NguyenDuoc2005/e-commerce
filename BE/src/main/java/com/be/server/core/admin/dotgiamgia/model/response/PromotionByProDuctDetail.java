package com.be.server.core.admin.dotgiamgia.model.response;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

public interface PromotionByProDuctDetail {
    @Value("#{target.image}")
    String getImage();
    @Value("#{target.code}")
    String getCode();
    @Value("#{target.name}")
    String getName();
    @Value("#{target.namePromotion}")
    String getNamePromotion();
    @Value("#{target.valuePromotion}")
    BigDecimal getValuePromotion();
    @Value("#{target.statusPromotion}")
    String getStatusPromotion();
}