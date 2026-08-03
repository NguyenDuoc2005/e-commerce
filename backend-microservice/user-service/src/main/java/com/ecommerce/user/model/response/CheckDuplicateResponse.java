package com.ecommerce.user.model.response;

public class CheckDuplicateResponse {

    private boolean exists;

    public CheckDuplicateResponse(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
