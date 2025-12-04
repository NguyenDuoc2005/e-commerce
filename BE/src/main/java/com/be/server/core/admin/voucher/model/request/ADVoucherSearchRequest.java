package com.be.server.core.admin.voucher.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.infrastructure.constant.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // Import lớp LocalDate

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADVoucherSearchRequest extends PageableRequest {

   private String q; // Trường tìm kiếm chung (mã/tên phiếu)

   private LocalDate startDate; // Trường 'Từ ngày'
   private LocalDate endDate;   // Trường 'Đến ngày'

   private Integer kieuGiam;

   private Integer status;
   private Boolean kieu;

   private EntityStatus entityStatus;
   private Boolean trangThai;

}