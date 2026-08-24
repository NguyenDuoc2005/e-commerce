package com.ecommerce.catalog.controller;

import com.ecommerce.common.base.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ResponseObject<?>> handleForbidden(SecurityException exception) {
        return new ResponseEntity<>(new ResponseObject<>(null, HttpStatus.FORBIDDEN, exception.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseObject<?>> handleConflict(DataIntegrityViolationException exception) {
        return new ResponseEntity<>(new ResponseObject<>(null, HttpStatus.CONFLICT, "CATALOG_CONSTRAINT_VIOLATION"), HttpStatus.CONFLICT);
    }
}
