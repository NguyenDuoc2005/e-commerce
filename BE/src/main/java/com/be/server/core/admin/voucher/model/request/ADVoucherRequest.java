package com.be.server.core.admin.voucher.model.request;

import com.be.server.infrastructure.constant.EntityVoucher;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ADVoucherRequest {


    private String id;

    private String ma;

    private String ten;

    private Double LoiPhanNay;

    private Integer soLuongPhieu;

    private Date ngayBatDau;

    private Date ngayKetThuc;

    private Double dieuKien;

    private Double giaGiam;

    private Boolean loaiGiam;

    private Boolean kieuGiam;

    private List<String> khachHangIds;
}
