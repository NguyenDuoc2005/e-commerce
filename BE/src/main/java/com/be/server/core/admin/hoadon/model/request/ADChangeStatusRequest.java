package com.be.server.core.admin.hoadon.model.request;

import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import lombok.Data;

@Data
public class ADChangeStatusRequest {

   private String maHoaDon;

   private EntityTrangThaiHoaDon status;

   private String note;
}