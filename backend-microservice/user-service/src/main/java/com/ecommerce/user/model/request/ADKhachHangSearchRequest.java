package com.ecommerce.user.model.request;

import com.ecommerce.common.base.PageableRequest;
import com.ecommerce.user.constant.EntityStatus;

public class ADKhachHangSearchRequest extends PageableRequest {

    private Integer status;

    private EntityStatus entityStatus;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public EntityStatus getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }
}
