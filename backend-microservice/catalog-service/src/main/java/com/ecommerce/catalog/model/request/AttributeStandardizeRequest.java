package com.ecommerce.catalog.model.request;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class AttributeStandardizeRequest {
    @NotBlank
    private String name;
    private String defaultUnit;
    private List<String> categoryIds = new ArrayList<>();
    private String reason;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }
    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
