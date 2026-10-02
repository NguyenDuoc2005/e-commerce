package com.ecommerce.catalog.model.request;

import java.math.BigDecimal;

public class ElasticsearchProductSearchRequest {
    private String q;
    private String categoryId;
    private String sellerId;
    private String color;
    private String sizeValue;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private int page = 0;
    private int size = 20;
    private String sort;
    private String cursor;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSizeValue() { return sizeValue; }
    public void setSizeValue(String sizeValue) { this.sizeValue = sizeValue; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public String getCursor() { return cursor; }
    public void setCursor(String cursor) { this.cursor = cursor; }
}
