package com.ecommerce.catalog.model.request;

import java.math.BigDecimal;

public class ProductSearchRequest {
    private String q = "";
    private String categoryId;
    private String attributeFilters;
    private String variantFilters;
    private String sellerId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort = "createdAt_desc";
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
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
