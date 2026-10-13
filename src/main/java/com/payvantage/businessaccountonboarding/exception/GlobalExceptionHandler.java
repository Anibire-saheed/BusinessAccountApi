package com.payvantage.businessaccountonboarding.exception;

import com.payvantage.businessaccountonboarding.data.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ApplicationNotFoundException.class, PrincipalNotFoundException.class, SignatoryNotFoundException.class})
    public ResponseEntity<BaseResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidApplicationStateException.class)
    public ResponseEntity<BaseResponse> handleInvalidState(InvalidApplicationStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(DocumentUploadException.class)
    public ResponseEntity<BaseResponse> handleUploadFailure(DocumentUploadException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse(HttpStatus.BAD_GATEWAY.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        BaseResponse response = errorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed");
        response.setErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private BaseResponse errorResponse(int statusCode, String description) {
        BaseResponse response = new BaseResponse();
        response.setStatusCode(statusCode);
        response.setDescription(description);
        return response;
    }
}
