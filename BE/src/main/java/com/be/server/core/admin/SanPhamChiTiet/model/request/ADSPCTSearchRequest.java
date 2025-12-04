package com.be.server.core.admin.SanPhamChiTiet.model.request;

import com.be.server.core.common.base.PageableRequest;


import com.be.server.infrastructure.constant.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.action.internal.EntityAction;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADSPCTSearchRequest extends PageableRequest {

   private String q;

   private String idSP;



   private String status;

   private String priceMin;

   private String priceMax;

   private String idMS;

   private String idKT;

   private EntityStatus entityStatus;
}



