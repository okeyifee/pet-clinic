package com.samuel.sniffers.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.samuel.sniffers.api.exception.*;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.api.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;
    private final Logger log;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.error("ResourceNotFoundException occurred: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, ex.getMessage(), null, null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized access exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(401, ex.getMessage(), null, null));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidRequest(InvalidRequestException ex) {
        log.error("Invalid request exception: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.error(400, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateTransitionException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalStateTransition(IllegalStateTransitionException ex) {
        log.error("Illegal transition exception: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.error(422, ex.getMessage(), null);
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
        log.error("Validation exception occurred: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.error(400,"Validation failed.", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public Object handleCustomerAlreadyExists(CustomerAlreadyExistsException ex) {
        log.error("Customer with name already exist");
        ApiResponse<String> response = ApiResponse.error(409, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }




//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<List<String>>> handleValidation(MethodArgumentNotValidException ex) {
//        List<String> errors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(error -> error.getField() + ": " + error.getDefaultMessage())
//                .toList();
//
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(new ApiResponse<>(400, "Validation failed", errors, DateTimeUtils.getCurrentDateTimeReadable()));
//    }

//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<ApiResponse<String>> handleInvalidJson(HttpMessageNotReadableException ex) {
//        String errorMessage = "Malformed JSON request";
//
//        log.error("Malformed request JSON received from API: {}", ex.getMessage());
//
//        ApiResponse<String> response = new ApiResponse<>(400, errorMessage, null, null);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }

//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidJson(HttpMessageNotReadableException ex) {
//        String errorMessage = "Malformed JSON request";
//
//        log.error("Malformed request JSON received from API: {}", ex.getMessage(), ex);
//
//        // Optionally include the error details in a map, or just the message
//        Map<String, String> errorDetails = new HashMap<>();
//        errorDetails.put("jsonError", "Invalid JSON format or extra fields");
//
//        // Return ApiResponse with error details
//        ApiResponse<Map<String, String>> response = new ApiResponse<>(400, errorMessage, null, errorDetails);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }

//    // Handle malformed JSON request and check for extra fields
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidJson(HttpMessageNotReadableException ex) {
//        // Log the exception details
//        String errorMessage = "Malformed JSON request";
//
//        // Get the raw error message from the exception
//        String errorDetails = ex.getMessage();
//        log.error("Malformed request JSON received from API: {}", errorDetails);
//
//        // Parse the raw error message to find extra fields (if present)
//        Map<String, String> extraFields = findExtraFields(errorDetails);
//
//        // Create the response with a detailed error message
//        String errorDetailMessage = extraFields.isEmpty() ? "No extra fields detected" :
//                "Request contains unrecognized fields: " + String.join(", ", extraFields.keySet());
//
//        // Return the error response with detailed message about extra fields
//        ApiResponse<Map<String, String>> response = new ApiResponse<>(400, errorMessage, null, Map.of("extraField", errorDetailMessage));
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }
//
//    // Parse the exception message to extract extra fields
//    private Map<String, String> findExtraFields(String errorDetails) {
//        Map<String, String> extraFields = new HashMap<>();
//        try {
//            // We know that the exception message contains "Unrecognized field" when extra fields are present
//            if (errorDetails.contains("Unrecognized field")) {
//                // Extract extra fields from the message
//                String[] parts = errorDetails.split("Unrecognized field(s)?: ");
//                if (parts.length > 1) {
//                    // Extract the extra field names from the message
//                    String extraFieldNames = parts[1].replaceAll("[{}\\\"]", "");
//                    String[] fieldNames = extraFieldNames.split(",");
//                    for (String field : fieldNames) {
//                        extraFields.put(field.trim(), "Extra field detected");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error parsing extra fields from the exception message: {}", e.getMessage());
//        }
//        return extraFields;
//    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Bad request: {}", ex.getMessage());
        if (ex.getCause() instanceof MismatchedInputException) {
            MismatchedInputException cause = (MismatchedInputException) ex.getCause();
            List<String> unknownFields = new ArrayList<>();

            // Get all unknown fields from the exception chain
            Throwable current = ex;
            while (current != null) {
                if (current instanceof UnrecognizedPropertyException) {
                    UnrecognizedPropertyException unrecognized = (UnrecognizedPropertyException) current;
                    unknownFields.add(unrecognized.getPropertyName());
                }
                current = current.getCause();
            }

            String message = String.format("Unknown field(s): %s. These fields are not recognized",
                    String.join(", ", unknownFields));

            ApiResponse<String> response = new ApiResponse<>(400, message, null, null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        ApiResponse<String> response = new ApiResponse<>(400, "Invalid JSON format in request", null, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
