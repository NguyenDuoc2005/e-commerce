package com.ecommerce.catalog.model.request;

public class ProductSearchRequest {
    private String q = "";
    private String categoryId;
    private String attributeFilters;
    private String variantFilters;
    private String sellerId;
    private int page;
    private int size = 20;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getAttributeFilters() { return attributeFilters; }
    public void setAttributeFilters(String attributeFilters) { this.attributeFilters = attributeFilters; }
    public String getVariantFilters() { return variantFilters; }
    public void setVariantFilters(String variantFilters) { this.variantFilters = variantFilters; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
