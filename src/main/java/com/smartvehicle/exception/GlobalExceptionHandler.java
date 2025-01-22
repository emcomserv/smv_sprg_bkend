package com.smartvehicle.exception;

import com.twilio.exception.ApiException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle Twilio ApiException
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleTwilioApiException(ApiException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Twilio Error", getRootCauseMessage(ex), request);
    }

    // Handle AuthenticationException
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", getRootCauseMessage(ex), request);
    }

    // Handle EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Entity Not Found", getRootCauseMessage(ex), request);
    }

    // Handle ConstraintViolationException
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Constraint Violation", getRootCauseMessage(ex), request);
    }

    // Handle DataIntegrityViolationException
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Data Integrity Violation", getRootCauseMessage(ex), request);
    }

    // Handle BadSqlGrammarException
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<Map<String, Object>> handleBadSqlGrammarException(BadSqlGrammarException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "SQL Syntax Error", getRootCauseMessage(ex), request);
    }

    // Handle EmptyResultDataAccessException
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "No Data Found", getRootCauseMessage(ex), request);
    }

    // Handle IOException
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "I/O Error", getRootCauseMessage(ex), request);
    }

    // Handle SQLException
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(SQLException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SQL Error", getRootCauseMessage(ex), request);
    }

    // Handle All Other Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", getRootCauseMessage(ex), request);
    }

    // Utility Method to Build Error Response
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message, WebRequest request) {
        log.error("Error: {}, Cause: {}", error, message);
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(body, status);
    }

    // Utility Method to Extract Root Cause
    private String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;
        log.error("Root cause stack trace:  ", rootCause);
        while (rootCause.getCause() != null && rootCause != rootCause.getCause()) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage() != null ? rootCause.getMessage() : "No detailed message available";
    }
}
