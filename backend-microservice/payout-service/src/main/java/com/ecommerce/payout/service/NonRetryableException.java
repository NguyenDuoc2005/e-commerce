package com.ecommerce.payout.service;
public class NonRetryableException extends RuntimeException { public NonRetryableException(String m, Throwable t){super(m,t);} public NonRetryableException(String m){super(m);} }
