package com.ecommerce.common.base;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ResponseObject<T> {

    private boolean isSuccess = false;

    private HttpStatus status;

    private T data;

    private String message;

    private Instant timestamp = Instant.now();

    public ResponseObject() {
    }

    public ResponseObject(T obj, HttpStatus status, String message) {
        processResponseObject(obj);
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void processResponseObject(T obj) {
        if (obj != null) {
            this.isSuccess = true;
            this.data = obj;
        }
    }

    public ResponseObject<T> success(T obj, String message) {
        processResponseObject(obj);
        this.status = HttpStatus.OK;
        this.message = message;
        return this;
    }

    public ResponseObject<T> success(String message) {
        this.isSuccess = true;
        this.status = HttpStatus.OK;
        this.message = message;
        return this;
    }

    public ResponseObject<T> error(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
        return this;
    }

    public static <T> ResponseObject<T> successForward(T obj, String message) {
        ResponseObject<T> responseObject = new ResponseObject<>();
        responseObject.setSuccess(true);
        responseObject.setStatus(HttpStatus.OK);
        responseObject.setData(obj);
        responseObject.setMessage(message);
        return responseObject;
    }

    public static <T> ResponseObject<T> errorForward(String message, HttpStatus status) {
        ResponseObject<T> responseObject = new ResponseObject<>();
        responseObject.setSuccess(false);
        responseObject.setStatus(status);
        responseObject.setMessage(message);
        return responseObject;
    }
}
