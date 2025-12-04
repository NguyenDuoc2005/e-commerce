package com.be.server.core.admin.chatlieu.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADChatLieuSearchRequest extends PageableRequest {

   private String q;

}
