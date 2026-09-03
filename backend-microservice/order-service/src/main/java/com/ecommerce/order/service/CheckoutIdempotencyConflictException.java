package com.ecommerce.order.service;

public class CheckoutIdempotencyConflictException extends RuntimeException {
    public CheckoutIdempotencyConflictException(String message) { super(message); }
}
