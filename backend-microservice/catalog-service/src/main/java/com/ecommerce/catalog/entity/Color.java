package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.CatalogAttribute;
import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Table(name = "color")
@DynamicUpdate
public class Color extends PrimaryEntity implements CatalogAttribute, Serializable {

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String mau;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMau() { return mau; }
    public void setMau(String mau) { this.mau = mau; }
}
