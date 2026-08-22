package com.ecommerce.catalog.model.request;

import com.ecommerce.catalog.constant.AttributeDataType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DynamicAttributeValueRequest {
    private String attributeId;
    private String name;
    private AttributeDataType dataType;
    private String textValue;
    private BigDecimal numberValue;
    private String unit;
    private List<String> optionValues = new ArrayList<>();
    private List<String> selectedOptionIds = new ArrayList<>();
    private List<String> selectedOptionValues = new ArrayList<>();
    private Integer displayOrder;

    public String getAttributeId() { return attributeId; }
    public void setAttributeId(String attributeId) { this.attributeId = attributeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AttributeDataType getDataType() { return dataType; }
    public void setDataType(AttributeDataType dataType) { this.dataType = dataType; }
    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }
    public BigDecimal getNumberValue() { return numberValue; }
    public void setNumberValue(BigDecimal numberValue) { this.numberValue = numberValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public List<String> getOptionValues() { return optionValues; }
    public void setOptionValues(List<String> optionValues) { this.optionValues = optionValues == null ? new ArrayList<>() : optionValues; }
    public List<String> getSelectedOptionIds() { return selectedOptionIds; }
    public void setSelectedOptionIds(List<String> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds == null ? new ArrayList<>() : selectedOptionIds; }
    public List<String> getSelectedOptionValues() { return selectedOptionValues; }
    public void setSelectedOptionValues(List<String> selectedOptionValues) { this.selectedOptionValues = selectedOptionValues == null ? new ArrayList<>() : selectedOptionValues; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
