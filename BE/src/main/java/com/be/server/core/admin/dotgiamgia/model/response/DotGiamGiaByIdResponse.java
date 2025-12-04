package com.be.server.core.admin.dotgiamgia.model.response;

import com.be.server.entity.DotGiamGia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.math.BigDecimal;

@Projection(types = DotGiamGia.class)
public interface DotGiamGiaByIdResponse {

    @Value("#{target.id}")
    String getId();
    @Value("#{target.code}")
    String getCode();
    @Value("#{target.name}")
    String getName();
    @Value("#{target.value}")
    BigDecimal getValue();
    @Value("#{target.startDate}")
    Long getStartDate();
    @Value("#{target.endDate}")
    Long getEndDate();
    @Value("#{target.status}")
    String getStatus();
    @Value("#{target.productDetail}")
    String getProductDetail();
    @Value("#{target.productDetailUpdate}")
    String getProductDetailUpdate();
    @Value("#{target.product}")
    String getProduct();
    @Value("#{target.promotionProductDetail}")
    String getPromotionProductDetail();
}