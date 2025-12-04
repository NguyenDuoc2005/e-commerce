package com.be.server.core.admin.thongke.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThongKeDonHangResponse {

    private String date;
    private Long soLuongDonHang;
}
