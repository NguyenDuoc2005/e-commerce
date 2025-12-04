package com.be.server.core.admin.dotgiamgia.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateDotGiamGiaRequest {

    @NotBlank(message = "Nhập tên khuyến mại")
    private String name;

    @NotNull(message = "Nhập giá trị khuyến mại")
    private Double value;

    @NotNull(message = "Nhập ngày bắt đầu khuyến mại")
    private Long startDate;

    @NotNull(message = "Nhập ngày kết thúc khuyến mại")
    private Long endDate;

    private List<IdProductDetail> idProductDetails =new ArrayList<>();
}