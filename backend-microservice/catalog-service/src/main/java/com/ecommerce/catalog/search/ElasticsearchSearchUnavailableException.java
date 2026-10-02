package com.ecommerce.catalog.search;

public class ElasticsearchSearchUnavailableException extends RuntimeException {
    public ElasticsearchSearchUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
