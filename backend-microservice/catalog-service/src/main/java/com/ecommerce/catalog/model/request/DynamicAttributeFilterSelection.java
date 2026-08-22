package com.ecommerce.catalog.model.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DynamicAttributeFilterSelection {
    private List<String> values = new ArrayList<>();
    private BigDecimal min;
    private BigDecimal max;

    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values == null ? new ArrayList<>() : values; }
    public BigDecimal getMin() { return min; }
    public void setMin(BigDecimal min) { this.min = min; }
    public BigDecimal getMax() { return max; }
    public void setMax(BigDecimal max) { this.max = max; }
}
