package com.ecommerce.catalog.controller;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.catalog.search.ElasticsearchSearchUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class CatalogExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseObject<?>> handleValidation(IllegalArgumentException exception) {
        ResponseObject<?> response = new ResponseObject<>(null, HttpStatus.BAD_REQUEST, exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseObject<?>> handleBeanValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("REQUEST_INVALID");
        return new ResponseEntity<>(new ResponseObject<>(null, HttpStatus.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseObject<?>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = "INVALID_QUERY_PARAMETER: " + exception.getName();
        return new ResponseEntity<>(new ResponseObject<>(null, HttpStatus.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ElasticsearchSearchUnavailableException.class)
    public ResponseEntity<ResponseObject<?>> handleSearchUnavailable(ElasticsearchSearchUnavailableException exception) {
        return new ResponseEntity<>(new ResponseObject<>(null, HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage()),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ResponseObject<?>> handleForbidden(SecurityException exception) {
        return new ResponseEntity<>(new ResponseObject<>(null, HttpStatus.FORBIDDEN, exception.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseObject<?>> handleConflict(DataIntegrityViolationException exception) {
        return new ResponseEntity<>(new ResponseObject<>(null, HttpStatus.CONFLICT, "CATALOG_CONSTRAINT_VIOLATION"), HttpStatus.CONFLICT);
    }
}
