package com.ecommerce.catalog.entity.base;

public interface CatalogAttribute {

    String getId();

    String getCode();

    void setCode(String code);

    String getName();

    void setName(String name);

    com.ecommerce.catalog.constant.EntityStatus getStatus();

    void setStatus(com.ecommerce.catalog.constant.EntityStatus status);
}
