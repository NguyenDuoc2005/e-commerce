package com.be.server.core.permitall.sanpham.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PMMauSacDTO {
    private String ten;
    private String maMau;
}
