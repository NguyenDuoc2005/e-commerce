package com.ecommerce.catalog.model.request;

import java.util.ArrayList;
import java.util.List;

public class AttributeStandardizeRequest {
    private String name;
    private List<String> categoryIds = new ArrayList<>();
    private boolean filterable;
    private String reason;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds == null ? new ArrayList<>() : categoryIds; }
    public boolean isFilterable() { return filterable; }
    public void setFilterable(boolean filterable) { this.filterable = filterable; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
