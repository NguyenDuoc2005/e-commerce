package com.ecommerce.common.util;

import com.ecommerce.common.base.ResponseObject;
import org.springframework.http.ResponseEntity;

public final class ResponseUtils {

    private ResponseUtils() {
    }

    public static ResponseEntity<?> createResponseEntity(ResponseObject<?> responseObject) {
        return new ResponseEntity<>(responseObject, responseObject.getStatus());
    }
}
