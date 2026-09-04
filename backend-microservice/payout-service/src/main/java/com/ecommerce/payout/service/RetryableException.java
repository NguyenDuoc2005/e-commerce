package com.ecommerce.payout.service;
public class RetryableException extends RuntimeException { public RetryableException(String m, Throwable t){super(m,t);} public RetryableException(String m){super(m);} }
