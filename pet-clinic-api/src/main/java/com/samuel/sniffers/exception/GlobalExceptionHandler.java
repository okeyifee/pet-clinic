package com.samuel.sniffers.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.samuel.sniffers.api.exception.*;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null, null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null, null));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidRequest(InvalidRequestException ex) {
        log.error("Invalid request: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateTransitionException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalStateTransition(IllegalStateTransitionException ex) {
        log.error("Illegal basket status transition: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Collect all validation errors
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        for (ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            errors.put(objectError.getObjectName(), objectError.getDefaultMessage());
        }

        // Return all validation errors
        log.error("Validation failed: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),"Validation failed.", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public Object handleCustomerAlreadyExists(CustomerAlreadyExistsException ex) {
        log.error("Customer with name already exists: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(StreamingException.class)
    public ResponseEntity<ApiResponse<String>> handleStreamingException(StreamingException ex) {
        log.error("Exception in the streaming API: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error processing streaming request: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        List<String> errors = new ArrayList<>();
        final String missingPayload = "Required request body is missing";
        final String invalidPayloadResponse = "Invalid JSON payload.";
        ApiResponse<List<String>> response;

        if (cause instanceof UnrecognizedPropertyException propertyException) {
            String fieldName = propertyException.getPropertyName();
            errors.add("Unknown field: '" + fieldName + "'. Allowed fields are: " +
                    propertyException.getKnownPropertyIds().toString());
            response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), invalidPayloadResponse, errors);
        } else {
            if (ex.getMessage().startsWith(missingPayload)) {
                // Limit exposing internals
                errors.add(missingPayload);
                response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),"Missing JSON payload.", errors);
            } else {
                errors.add("Invalid JSON: " + ex.getMessage());
                response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),invalidPayloadResponse, errors);
            }
        }

        log.error("Invalid JSON payload: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException occurred: {}", ex.getMessage());

        List<String> errors = new ArrayList<>();
        errors.add("The API endpoint does not support '" + ex.getMethod() + "' requests.");

        ApiResponse<List<String>> response = ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "Error occurred.", errors);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleGenericException(Exception ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errors.put("error", "Internal Server Error");
        errors.put("message", ex.getMessage());

        log.error("Exception happened: {}", ex.getMessage());
        ApiResponse<Map<String, Object>> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred.", errors);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
