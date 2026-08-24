package com.ecommerce.catalog.model.request;

import com.ecommerce.catalog.constant.AttributeDataType;
import com.ecommerce.catalog.constant.EntityStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductAggregateRequest {
    @NotBlank
    private String categoryId;
    private String code;
    @NotBlank
    private String name;
    private String description;
    @Valid
    private List<ImageInput> productImages = new ArrayList<>();
    @Valid
    private List<AttributeInput> attributes = new ArrayList<>();
    @Valid
    private List<AxisInput> variantAxes = new ArrayList<>();
    @Valid
    private List<VariantInput> variants = new ArrayList<>();

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<ImageInput> getProductImages() { return productImages; }
    public void setProductImages(List<ImageInput> productImages) { this.productImages = productImages; }
    public List<AttributeInput> getAttributes() { return attributes; }
    public void setAttributes(List<AttributeInput> attributes) { this.attributes = attributes; }
    public List<AxisInput> getVariantAxes() { return variantAxes; }
    public void setVariantAxes(List<AxisInput> variantAxes) { this.variantAxes = variantAxes; }
    public List<VariantInput> getVariants() { return variants; }
    public void setVariants(List<VariantInput> variants) { this.variants = variants; }

    public static class ImageInput {
        private String id;
        @NotBlank
        private String url;
        @Min(0)
        private Integer displayOrder = 0;
        private EntityStatus status = EntityStatus.ACTIVE;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        public EntityStatus getStatus() { return status; }
        public void setStatus(EntityStatus status) { this.status = status; }
    }

    public static class AttributeInput {
        private String definitionId;
        private String name;
        @NotNull
        private AttributeDataType dataType;
        private String valueText;
        private BigDecimal valueNumber;
        private String unit;
        private List<String> selectedOptionIds = new ArrayList<>();
        private List<String> selectedOptionValues = new ArrayList<>();
        @Min(0)
        private Integer displayOrder = 0;

        public String getDefinitionId() { return definitionId; }
        public void setDefinitionId(String definitionId) { this.definitionId = definitionId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public AttributeDataType getDataType() { return dataType; }
        public void setDataType(AttributeDataType dataType) { this.dataType = dataType; }
        public String getValueText() { return valueText; }
        public void setValueText(String valueText) { this.valueText = valueText; }
        public BigDecimal getValueNumber() { return valueNumber; }
        public void setValueNumber(BigDecimal valueNumber) { this.valueNumber = valueNumber; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public List<String> getSelectedOptionIds() { return selectedOptionIds; }
        public void setSelectedOptionIds(List<String> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds; }
        public List<String> getSelectedOptionValues() { return selectedOptionValues; }
        public void setSelectedOptionValues(List<String> selectedOptionValues) { this.selectedOptionValues = selectedOptionValues; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }

    public static class AxisInput {
        private String id;
        @NotBlank
        private String clientKey;
        @NotBlank
        private String name;
        private String nameSuggestionId;
        @NotNull
        private Integer displayOrder;
        @Valid
        private List<AxisValueInput> values = new ArrayList<>();

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getClientKey() { return clientKey; }
        public void setClientKey(String clientKey) { this.clientKey = clientKey; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNameSuggestionId() { return nameSuggestionId; }
        public void setNameSuggestionId(String nameSuggestionId) { this.nameSuggestionId = nameSuggestionId; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        public List<AxisValueInput> getValues() { return values; }
        public void setValues(List<AxisValueInput> values) { this.values = values; }
    }

    public static class AxisValueInput {
        private String id;
        @NotBlank
        private String clientKey;
        @NotBlank
        private String value;
        @Min(0)
        private Integer displayOrder = 0;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getClientKey() { return clientKey; }
        public void setClientKey(String clientKey) { this.clientKey = clientKey; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }

    public static class VariantInput {
        private String id;
        @NotBlank
        private String sku;
        @NotNull
        @DecimalMin("0")
        private BigDecimal salePrice;
        @NotNull
        @Min(0)
        private Integer quantity;
        private String imageUrl;
        private boolean defaultVariant;
        private EntityStatus status = EntityStatus.ACTIVE;
        private List<String> selectionValueKeys = new ArrayList<>();

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public BigDecimal getSalePrice() { return salePrice; }
        public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public boolean isDefaultVariant() { return defaultVariant; }
        public void setDefaultVariant(boolean defaultVariant) { this.defaultVariant = defaultVariant; }
        public EntityStatus getStatus() { return status; }
        public void setStatus(EntityStatus status) { this.status = status; }
        public List<String> getSelectionValueKeys() { return selectionValueKeys; }
        public void setSelectionValueKeys(List<String> selectionValueKeys) { this.selectionValueKeys = selectionValueKeys; }
    }
}
