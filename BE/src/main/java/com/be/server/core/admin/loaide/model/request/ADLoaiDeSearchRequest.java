package com.be.server.core.admin.loaide.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.infrastructure.constant.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADLoaiDeSearchRequest extends PageableRequest {

   private String q;

   private Integer status;

   private EntityStatus entityStatus;

}
