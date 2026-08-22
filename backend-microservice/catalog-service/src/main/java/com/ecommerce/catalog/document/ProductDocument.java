package com.ecommerce.catalog.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Keyword)
    private String brandId;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    @Field(type = FieldType.Text)
    private String sellerName;

    @Field(type = FieldType.Keyword)
    private String sellerSlug;

    @Field(type = FieldType.Double)
    private Double sellerRating;

    @Field(type = FieldType.Long)
    private Long soldCount;

    @Field(type = FieldType.Nested)
    private List<AttributeDocument> attributes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerSlug() { return sellerSlug; }
    public void setSellerSlug(String sellerSlug) { this.sellerSlug = sellerSlug; }
    public Double getSellerRating() { return sellerRating; }
    public void setSellerRating(Double sellerRating) { this.sellerRating = sellerRating; }
    public Long getSoldCount() { return soldCount; }
    public void setSoldCount(Long soldCount) { this.soldCount = soldCount; }
    public List<AttributeDocument> getAttributes() { return attributes; }
    public void setAttributes(List<AttributeDocument> attributes) { this.attributes = attributes; }

    public static class AttributeDocument {
        @Field(type = FieldType.Keyword)
        private String attributeId;

        @Field(type = FieldType.Text)
        private String name;

        @Field(type = FieldType.Keyword)
        private String dataType;

        @Field(type = FieldType.Text)
        private List<String> textValues;

        @Field(type = FieldType.Keyword)
        private List<String> keywordValues;

        @Field(type = FieldType.Double)
        private Double numberValue;

        @Field(type = FieldType.Keyword)
        private List<String> optionIds;

        public String getAttributeId() { return attributeId; }
        public void setAttributeId(String attributeId) { this.attributeId = attributeId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public List<String> getTextValues() { return textValues; }
        public void setTextValues(List<String> textValues) { this.textValues = textValues; }
        public List<String> getKeywordValues() { return keywordValues; }
        public void setKeywordValues(List<String> keywordValues) { this.keywordValues = keywordValues; }
        public Double getNumberValue() { return numberValue; }
        public void setNumberValue(Double numberValue) { this.numberValue = numberValue; }
        public List<String> getOptionIds() { return optionIds; }
        public void setOptionIds(List<String> optionIds) { this.optionIds = optionIds; }
    }
}
