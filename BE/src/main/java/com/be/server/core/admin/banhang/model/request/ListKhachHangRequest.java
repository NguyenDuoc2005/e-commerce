package com.be.server.core.admin.banhang.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListKhachHangRequest extends PageableRequest {

    private String q; // Từ khóa tìm kiếm

}
