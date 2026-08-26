package com.ecommerce.promotion.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FlashSaleCampaignRequest {
    @NotBlank private String name;
    private String description;
    @NotNull private Long registrationStartDate;
    @NotNull private Long registrationEndDate;
    @NotNull private Long startDate;
    @NotNull private Long endDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(Long registrationStartDate) { this.registrationStartDate = registrationStartDate; }
    public Long getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(Long registrationEndDate) { this.registrationEndDate = registrationEndDate; }
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
}
