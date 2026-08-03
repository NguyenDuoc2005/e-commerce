package com.ecommerce.catalog.entity.base;

public interface CatalogAttribute {

    String getId();

    String getMa();

    void setMa(String ma);

    String getTen();

    void setTen(String ten);

    com.ecommerce.catalog.constant.EntityStatus getStatus();

    void setStatus(com.ecommerce.catalog.constant.EntityStatus status);
}
